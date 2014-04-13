package de.espend.idea.php.annotation.extension;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationReferencesProvider;
import de.espend.idea.php.annotation.extension.references.PhpClassFieldReference;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.Nullable;


public class DoctrineAnnotationFieldProvider implements PhpAnnotationReferencesProvider {

    @Nullable
    @Override
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter) {

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
            new PhpClassFieldReference((StringLiteralExpression) annotationPropertyParameter.getElement(), phpClass)
        };

    }

}
