package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpAnnotationReferenceProviderParameter {

    final private PsiElement psiElement;
    final private ProcessingContext processingContext;

    public PhpAnnotationReferenceProviderParameter(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        this.psiElement = psiElement;
        this.processingContext = processingContext;
    }

    @NotNull
    public PsiElement getPsiElement() {
        return psiElement;
    }

    @NotNull
    public ProcessingContext getProcessingContext() {
        return processingContext;
    }
}
