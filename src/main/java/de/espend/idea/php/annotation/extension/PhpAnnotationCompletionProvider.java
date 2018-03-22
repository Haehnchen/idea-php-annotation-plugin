package de.espend.idea.php.annotation.extension;

import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface PhpAnnotationCompletionProvider {
    void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter);
}
