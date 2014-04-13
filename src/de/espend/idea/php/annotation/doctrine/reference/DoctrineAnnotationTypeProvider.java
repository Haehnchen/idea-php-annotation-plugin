package de.espend.idea.php.annotation.doctrine.reference;

import com.intellij.psi.*;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import de.espend.idea.php.annotation.doctrine.reference.references.DoctrineRepositoryReference;
import org.jetbrains.annotations.Nullable;


public class DoctrineAnnotationTypeProvider implements PhpAnnotationReferenceProvider {

    @Nullable
    @Override
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, PhpAnnotationReferenceProviderParameter referencesByElementParameter) {

        if(annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null;
        }

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null || !propertyName.equals("repositoryClass")) {
            return null;
        }

        String presentableFQN = annotationPropertyParameter.getPhpClass().getPresentableFQN();
        if(!PhpLangUtil.equalsClassNames("Doctrine\\ORM\\Mapping\\Entity", presentableFQN)) {
            return null;
        }

        return new PsiReference[] {
            new DoctrineRepositoryReference((StringLiteralExpression) annotationPropertyParameter.getElement())
        };

    }

}
