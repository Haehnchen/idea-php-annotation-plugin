package de.espend.idea.php.annotation.reference.parameter;

import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ReferencesByElementParameter {

    private PsiElement psiElement;
    private ProcessingContext processingContext;

    public ReferencesByElementParameter(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
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
