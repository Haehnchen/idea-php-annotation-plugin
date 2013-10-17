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

        // now we get a call now, but also an error "Cannot find manipulator for PhpDocTag"

        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PhpDocTag.class),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

                    if(!(element instanceof PhpDocTag)) {
                        return new PsiReference[0];
                    }

                    return new PsiReference[] {
                        new PhpDocTagReference(element)
                    };
                }
            }

        );

    }

    public class PhpDocTagReference extends PsiPolyVariantReferenceBase<PsiElement> {

        public PhpDocTagReference(PsiElement psiElement) {
            super(psiElement);
        }

        /*
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


            return false;
        } */

        @NotNull
        @Override
        public ResolveResult[] multiResolve(boolean b) {

            if(!(getElement() instanceof PhpDocTag)) {
                return new ResolveResult[0];
            }

            PhpClass phpClass = AnnotationUtil.getAnnotationReference((PhpDocTag) getElement());
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
