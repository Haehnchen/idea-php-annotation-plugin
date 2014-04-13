package de.espend.idea.php.annotation.reference;

import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import de.espend.idea.php.annotation.reference.references.PhpClassReference;
import org.jetbrains.annotations.Nullable;

public class AnnotationClassProvider implements PhpAnnotationReferenceProvider {

    @Nullable
    @Override
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, PhpAnnotationReferenceProviderParameter referencesByElementParameter) {

        if(annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null;
        }

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null || !propertyName.equals("targetEntity")) {
            return null;
        }

        return new PsiReference[] {
            new PhpClassReference((StringLiteralExpression) annotationPropertyParameter.getElement())
        };

    }

}
