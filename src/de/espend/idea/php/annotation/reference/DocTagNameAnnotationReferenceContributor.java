package de.espend.idea.php.annotation.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PluginUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DocTagNameAnnotationReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar) {

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

                    return new PsiReference[] {
                        new PhpDocTagReference((PhpDocTag) element)
                    };
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

}
