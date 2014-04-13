package de.espend.idea.php.annotation.extension;

import com.intellij.psi.PsiReference;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import org.jetbrains.annotations.Nullable;

public interface PhpAnnotationReferenceProvider {
    @Nullable
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, PhpAnnotationReferenceProviderParameter referencesByElementParameter);
}
