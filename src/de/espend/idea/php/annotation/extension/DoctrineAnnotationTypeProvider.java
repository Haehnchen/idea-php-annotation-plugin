package de.espend.idea.php.annotation.extension;

import com.intellij.psi.*;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationReferencesProvider;
import de.espend.idea.php.annotation.extension.references.DoctrineRepositoryReference;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import org.jetbrains.annotations.Nullable;


public class DoctrineAnnotationTypeProvider implements PhpAnnotationReferencesProvider {

    @Nullable
    @Override
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter) {

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
