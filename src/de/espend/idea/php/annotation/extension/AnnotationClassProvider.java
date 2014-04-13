package de.espend.idea.php.annotation.extension;

import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationReferencesProvider;
import de.espend.idea.php.annotation.extension.references.PhpClassReference;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import org.jetbrains.annotations.Nullable;

public class AnnotationClassProvider implements PhpAnnotationReferencesProvider {

    @Nullable
    @Override
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null || !propertyName.equals("targetEntity")) {
            return null;
        }

        return new PsiReference[] {
            new PhpClassReference((StringLiteralExpression) annotationPropertyParameter.getElement())
        };

    }

}
