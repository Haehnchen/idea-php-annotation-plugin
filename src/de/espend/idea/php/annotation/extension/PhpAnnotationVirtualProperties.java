package de.espend.idea.php.annotation.extension;

import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyCompletionParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyTargetsParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Adds virtual fields for given class @Foo(foo='')
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface PhpAnnotationVirtualProperties {

    /**
     * Add completion elements
     */
    void addCompletions(@NotNull AnnotationVirtualPropertyCompletionParameter virtualPropertyParameter, @NotNull AnnotationCompletionProviderParameter parameter);

    /**
     * Add targets for given element
     */
    void getTargets(@NotNull AnnotationVirtualPropertyTargetsParameter parameter);
}
