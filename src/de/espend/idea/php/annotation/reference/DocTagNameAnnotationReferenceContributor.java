package de.espend.idea.php.annotation.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

public class DocTagNameAnnotationReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar) {

        // both dont get called on @Template
        // PlatformPatterns.psiElement()
        // PlatformPatterns.psiElement().withElementType(PhpDocElementTypes.DOC_TAG_NAME)

        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement().withElementType(PhpDocElementTypes.DOC_TAG_NAME),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

                    if(!(element instanceof PhpDocTag)) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[] {
                        new PhpDocTagReference((PhpDocTag) element)
                    };
                }
            }

        );

    }

    public class PhpDocTagReference extends PsiPolyVariantReferenceBase<PhpDocTag> {

        public PhpDocTagReference(PhpDocTag psiElement) {
            super(psiElement);
        }

        @Override
        public boolean isReferenceTo(PsiElement element) {

            // implemented this when usable
            /**
            if (element instanceof PhpUse) {
                final ResolveResult[] results = multiResolve(false);
                for (ResolveResult result : results) {
                    if(result.isValidResult() && result.getElement() == element) {
                        return true;
                    }
                }
            }
            */

            return false;
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
    }

}
