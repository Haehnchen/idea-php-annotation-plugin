package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationGlobalNamespacesLoaderParameter {
    @NotNull
    private final Project project;

    @NotNull
    public Project getProject() {
        return project;
    }

    public AnnotationGlobalNamespacesLoaderParameter(@NotNull Project project) {
        this.project = project;
    }
}
