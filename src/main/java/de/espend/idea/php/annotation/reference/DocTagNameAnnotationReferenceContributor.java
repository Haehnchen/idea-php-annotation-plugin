package de.espend.idea.php.annotation.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocToken;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.Variable;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
         * Collects static identifier elements on the first element and search them inside the use statements or global namespace
         *
         * - @Foo(F<caret>OO::BAR)
         * - @Foo(Fo<caret>o\FOO::BAR)
         */
        psiReferenceRegistrar.registerReferenceProvider(PlatformPatterns.psiElement(PhpDocToken.class), new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                if (element.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER) {
                    return PsiReference.EMPTY_ARRAY;
                }

                PsiElement prevSibling = element.getPrevSibling();
                if (prevSibling == null || prevSibling.getNode().getElementType() == PhpDocTokenTypes.DOC_NAMESPACE || PhpDocUtil.isDocStaticElement(prevSibling)) {
                    return PsiReference.EMPTY_ARRAY;
                }

                // We must be at first namespace part: "F<caret>oo\Bar::class"
                String namespaceForDocIdentifierAtStart = PhpDocUtil.getNamespaceForDocIdentifierAtStart(element);
                if (namespaceForDocIdentifierAtStart == null) {
                    return PsiReference.EMPTY_ARRAY;
                }

                PhpDocTag docTag = PhpPsiUtil.getParentByCondition(element, PhpDocTag.INSTANCEOF);
                if (docTag == null) {
                    return PsiReference.EMPTY_ARRAY;
                }

                // Find any import which is related here: "use Foo" => "F<caret>oo\Bar::class"
                String classFromDocIdentifierAsString = AnnotationUtil.getClassFromDocIdentifierAsString(element);
                if (classFromDocIdentifierAsString == null) {
                    return PsiReference.EMPTY_ARRAY;
                }

                return new PsiReference[]{new PhpDocIdentifierReference(element, classFromDocIdentifierAsString)};
            }
        });
    }

    private static class PhpDocTagReference extends PsiPolyVariantReferenceBase<PhpDocTag> {

        public PhpDocTagReference(PhpDocTag psiElement) {
            super(psiElement);
        }

        @Override
        public boolean isReferenceTo(@NotNull PsiElement element) {

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
        private final String fqn;

        PhpDocIdentifierReference(@NotNull PsiElement element, @NotNull String fqn) {
            super(element);
            this.fqn = fqn;
        }

        @NotNull
        @Override
        public TextRange getRangeInElement() {
            return TextRange.create(0, myElement.getTextLength());
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
        public boolean isReferenceTo(@NotNull PsiElement psiElement) {
            if(!(psiElement instanceof PhpNamedElement)) {
                return false;
            }

            String text = getElement().getText();
            if(StringUtils.isBlank(text)) {
                return false;
            }

            String classByContext = getFqnForClassNameByContext(myElement, text);
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

    /**
     * Resolve classname with scoped namespace imports on inside PhpDocTag
     *
     * @param psiElement PhpDocTag scoped element
     * @param className with namespace
     */
    @Nullable
    private static String getFqnForClassNameByContext(@NotNull PsiElement psiElement, @NotNull String className) {
        PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag.class);
        if(phpDocTag == null) {
            return null;
        }

        return AnnotationUtil.getUseImportMap(phpDocTag).get(className);
    }
}
