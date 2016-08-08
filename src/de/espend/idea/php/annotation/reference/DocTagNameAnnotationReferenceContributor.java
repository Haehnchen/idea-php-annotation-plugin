package de.espend.idea.php.annotation.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.annotation.util.PluginUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DocTagNameAnnotationReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {

        /**
         * Our main reference provider to attach DocBlocTag to their use declaration
         * This one resolve the "Optimize Usage" issues
         *
         * "@Template()", "@ORM\PostPersist()"
         */
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PhpDocTag.class),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

                    if(!PluginUtil.isEnabled(element) || !(element instanceof PhpDocTag)) {
                        return new PsiReference[0];
                    }

                    final Collection<PsiReference> references = ContainerUtil.newSmartList(
                        new PhpDocTagReference((PhpDocTag) element)
                    );

                    // @Foo(Fo<caret>o::FooBar)
                    collectStaticDocClassNameElement(element, references);

                    return references.toArray(new PsiReference[references.size()]);
                }

                /**
                 * Collects static identifier elements: @Foo(F<caret>OO::BAR)
                 */
                private void collectStaticDocClassNameElement(@NotNull PsiElement element, @NotNull Collection<PsiReference> references) {
                    PsiElement attributes = PhpPsiUtil.getChildOfType(element, PhpDocElementTypes.phpDocAttributeList);
                    if (attributes == null) {
                        return;
                    }

                    // @Foo(Foobar::CONST) and workaround for @Foo(name{Foobar::CONST}) as this are text elements
                    PsiElement[] psiElements = PsiTreeUtil.collectElements(element, PhpDocUtil::isDocStaticElement);

                    for (PsiElement psiElement : psiElements) {
                        PsiElement prevSibling = psiElement.getPrevSibling();
                        if(prevSibling == null) {
                            continue;
                        }

                        String text = prevSibling.getText();
                        if(StringUtils.isBlank(text)) {
                            continue;
                        }

                        PhpClass phpClass = PhpElementsUtil.getClassByContext(psiElement, text);
                        if(phpClass == null) {
                            continue;
                        }

                        references.add(new PhpDocIdentifierReference(psiElement.getPrevSibling(), phpClass.getFQN()));
                    }
                }
            }
        );
    }

    private static class PhpDocTagReference extends PsiPolyVariantReferenceBase<PhpDocTag> {

        public PhpDocTagReference(PhpDocTag psiElement) {
            super(psiElement);
        }

        @Override
        public boolean isReferenceTo(PsiElement element) {

            // use Doctrine\ORM\Mapping as "ORM";
            if (element instanceof PhpUse) {
                String useName = ((PhpUse) element).getName();
                String docUseName = getDocBlockName();

                if(useName.equals(docUseName)) {
                    return true;
                }

            }

            // eg for "Optimize Imports"
            // attach reference to @Template()
            // reference can also point to a namespace e.g. @Annotation\Exclude()
            if (element instanceof PhpNamedElement) {
                if(((PhpNamedElement) element).getName().equals(getDocBlockName())) {
                    return true;
                }
            }

            return false;
        }

        /**
         * We need to strip @ char before DocTag @Test, @Test\Foo
         *
         * @return TextRange of DocTag without @ char
         */
        public TextRange getRangeInElement() {
            String tagName = getElement().getName();
            int rangeStart = 0;
            int rangeEnd = tagName.length();

            // remove DocTag "@" char
            // it should always be true, check for security reason
            if(tagName.startsWith("@")) {
                rangeStart = 1;
                tagName = tagName.substring(1);
            }

            // "@ORM\PostPersist()"
            // only on alias and namespace use main ns
            if(tagName.contains("\\")) {
                rangeEnd = tagName.indexOf("\\") + rangeStart;
            }

            return new TextRange(rangeStart, rangeEnd);
        }

        @NotNull
        @Override
        public ResolveResult[] multiResolve(boolean b) {

            PhpClass phpClass = AnnotationUtil.getAnnotationReference(getElement());
            if(phpClass == null) {
                return new ResolveResult[0];
            }

            return new ResolveResult[] { new PsiElementResolveResult(phpClass) };
        }

        @NotNull
        @Override
        public Object[] getVariants() {
            return new Object[0];
        }

        /**
         * Get the class alias
         *
         * "@Template()"
         * "@ORM\PostPersist()"
         */
        private String getDocBlockName() {
            String docBlockName = getElement().getName();

            if(docBlockName.startsWith("@")) {
                docBlockName = docBlockName.substring(1);
            }

            if(docBlockName.contains("\\")) {
                docBlockName = docBlockName.substring(0, docBlockName.indexOf("\\"));
            }

            return docBlockName;
        }
    }

    /**
     * Adds support for references of "@Foobar(name=Fo<caret>oBar::Const)"
     */
    private static class PhpDocIdentifierReference extends PsiReferenceBase<PsiElement> {

        @NotNull
        private final PsiElement element;

        @NotNull
        private final String fqn;

        PhpDocIdentifierReference(@NotNull PsiElement element, @NotNull String fqn) {
            super(element);
            this.element = element;
            this.fqn = fqn;
        }

        /**
         * DocTag is our reference host; need to extract text range relatively from this element
         * Given: @Foobar(name=Fo<caret>oBar::Const)
         *
         * @return suffixed range
         */
        @Override
        public TextRange getRangeInElement() {
            // every item is wrapped into attribute list first
            PsiElement attributeList = element.getParent();
            if(!(attributeList instanceof PhpPsiElement)) {
                return null;
            }

            // offset of attribute list + docblock itself
            int startOffsetInParent = element.getStartOffsetInParent() + attributeList.getStartOffsetInParent();

            return new TextRange(startOffsetInParent, startOffsetInParent + element.getTextLength());
        }

        @Nullable
        @Override
        public PsiElement resolve() {
            return null;
        }

        /**
         * Attach element identify name to class of "use" usage
         *
         * @param psiElement PhpClass used in "use" statement
         */
        @Override
        public boolean isReferenceTo(PsiElement psiElement) {
            if(!(psiElement instanceof PhpNamedElement)) {
                return false;
            }

            String text = getElement().getText();
            if(StringUtils.isBlank(text)) {
                return false;
            }

            PsiElement namespace = element.getPrevSibling();
            if(PhpPsiUtil.isOfType(namespace, PhpDocTokenTypes.DOC_NAMESPACE)) {
                // @TODO: namespace not supported
                return false;
            }

            String classByContext = PhpElementsUtil.getFqnForClassNameByContext(element, text);
            if(classByContext != null) {
                return StringUtils.stripStart(((PhpNamedElement) psiElement).getFQN(), "\\")
                    .equalsIgnoreCase(StringUtils.stripStart(fqn, "\\"));
            }

            return false;
        }

        @NotNull
        @Override
        public Object[] getVariants() {
            return new Object[0];
        }
    }
}