package de.espend.idea.php.annotation;

import de.espend.idea.php.annotation.completion.parameter.CompletionParameter;

public interface PhpAnnotationCompletionProvider {
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter);
}
