package de.espend.idea.php.annotation.extension;

import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationExtension;
import de.espend.idea.php.annotation.completion.parameter.CompletionParameter;
import de.espend.idea.php.annotation.extension.references.PhpClassFieldReference;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DoctrineAnnotationFieldProvider implements PhpAnnotationExtension {

    @Nullable
    @Override
    public Collection<String> getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter) {
        return null;
    }

    @Nullable
    @Override
    public Collection<PsiReference> getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter) {

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

        return new ArrayList<PsiReference>(Arrays.asList(
            new PhpClassFieldReference((StringLiteralExpression) annotationPropertyParameter.getElement(), phpClass)
        ));
    }

}
