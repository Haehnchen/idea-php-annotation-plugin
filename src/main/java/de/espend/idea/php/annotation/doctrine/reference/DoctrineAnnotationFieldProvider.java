package de.espend.idea.php.annotation.doctrine.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.doctrine.reference.references.DoctrinePhpClassFieldReference;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineAnnotationFieldProvider implements PhpAnnotationReferenceProvider {

    @Nullable
    @Override
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, PhpAnnotationReferenceProviderParameter referencesByElementParameter) {

        if(annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null;
        }

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null || !(propertyName.equals("mappedBy") || propertyName.equals("inversedBy"))) {
            return null;
        }

        PsiElement parent = annotationPropertyParameter.getElement().getParent();

        StringLiteralExpression targetEntity = PhpElementsUtil.getChildrenOnPatternMatch(parent, AnnotationPattern.getPropertyIdentifierValue("targetEntity"));
        if(targetEntity == null) {
            return null;
        }

        PhpClass phpClass = PhpElementsUtil.getClassInsideAnnotation(targetEntity);
        if(phpClass == null) {
            return null;
        }

        return new PsiReference[] {
            new DoctrinePhpClassFieldReference((StringLiteralExpression) annotationPropertyParameter.getElement(), phpClass)
        };

    }

}
