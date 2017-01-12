package de.espend.idea.php.annotation.extension;

import de.espend.idea.php.annotation.extension.parameter.AnnotationGlobalNamespacesLoaderParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface PhpAnnotationGlobalNamespacesLoader {
    @NotNull
    Collection<String> getGlobalNamespaces(@NotNull AnnotationGlobalNamespacesLoaderParameter parameter);
}
