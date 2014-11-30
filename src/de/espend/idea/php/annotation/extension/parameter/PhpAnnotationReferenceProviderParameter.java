package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpAnnotationReferenceProviderParameter {

    private PsiElement psiElement;
    private ProcessingContext processingContext;

    public PhpAnnotationReferenceProviderParameter(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        this.psiElement = psiElement;
        this.processingContext = processingContext;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }

}
