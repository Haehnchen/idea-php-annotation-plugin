package de.espend.idea.php.annotation;

import com.intellij.psi.PsiReference;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import org.jetbrains.annotations.Nullable;

public interface PhpAnnotationReferencesProvider {
    @Nullable
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter);
}
