package de.espend.idea.php.annotation;

import com.intellij.psi.PsiReference;
import de.espend.idea.php.annotation.completion.parameter.CompletionParameter;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface PhpAnnotationExtension {

    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter);

    @Nullable
    public Collection<PsiReference> getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter);

}
