package de.espend.idea.php.annotation.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocToken;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
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

        /*
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
                    if(!(element instanceof PhpDocTag)) {
                        return new PsiReference[0];
                    }
                    return new PsiReference[]{new PhpDocTagReference((PhpDocTag) element)};
                }
            }
        );

        /*
          Collects static identifier elements: @Foo(F<caret>OO::BAR)
         */
        psiReferenceRegistrar.registerReferenceProvider(PlatformPatterns.psiElement(PhpDocToken.class), new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                String text = element.getText();
                if (StringUtils.isBlank(text)) return PsiReference.EMPTY_ARRAY;
                if (!PhpDocUtil.isDocStaticElement(element.getNextSibling())) return PsiReference.EMPTY_ARRAY;

                PsiElement docTag = PhpPsiUtil.getParentByCondition(element, PhpDocTag.INSTANCEOF);
                if (docTag == null) return PsiReference.EMPTY_ARRAY;

                PsiElement attributes = PhpPsiUtil.getChildOfType(docTag, PhpDocElementTypes.phpDocAttributeList);
                if (attributes == null) return PsiReference.EMPTY_ARRAY;

                PhpClass phpClass = PhpElementsUtil.getClassByContext(element.getNextSibling(), text);
                if (phpClass == null) return PsiReference.EMPTY_ARRAY;

                return new PsiReference[]{new PhpDocIdentifierReference(element, phpClass.getFQN())};
            }
        });
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
            if (element instanceof PhpNamedElement && !(element instanceof Variable)) {
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

        @NotNull
        @Override
        public TextRange getRangeInElement() {
            return TextRange.create(0, element.getTextLength());
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
