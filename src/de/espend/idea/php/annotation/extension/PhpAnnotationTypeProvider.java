package de.espend.idea.php.annotation.extension;

import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.Field;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationExtension;
import de.espend.idea.php.annotation.completion.parameter.CompletionParameter;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;


public class PhpAnnotationTypeProvider implements PhpAnnotationExtension {

    @Nullable
    @Override
    public Collection<String> getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();

        if(!annotationPropertyParameter.getType().equals(AnnotationPropertyParameter.Type.STRING) && propertyName == null) {
            return null;
        }

        for(Field field: annotationPropertyParameter.getPhpClass().getFields()) {
            if(field.getName().equals(propertyName)) {
                if(field.getType().toString().equals("bool")) {
                    return Arrays.asList("false", "true");
                }
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Collection<PsiReference> getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter) {
        return null;
    }
}
