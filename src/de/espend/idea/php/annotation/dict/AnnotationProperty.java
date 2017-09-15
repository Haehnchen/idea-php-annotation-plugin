package de.espend.idea.php.annotation.dict;

import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationProperty {
    @NotNull
    final private String propertyName;

    @NotNull
    final private AnnotationPropertyEnum annotationPropertyEnum;

    public AnnotationProperty(@NotNull String propertyName, @NotNull AnnotationPropertyEnum annotationPropertyEnum) {
        this.propertyName = propertyName;
        this.annotationPropertyEnum = annotationPropertyEnum;
    }

    @NotNull
    public String getPropertyName() {
        return propertyName;
    }

    @NotNull
    public AnnotationPropertyEnum getAnnotationPropertyEnum() {
        return annotationPropertyEnum;
    }
}
