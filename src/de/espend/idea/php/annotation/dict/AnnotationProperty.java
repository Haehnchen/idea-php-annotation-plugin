package de.espend.idea.php.annotation.dict;

public class AnnotationProperty {

    private String propertyName;
    private AnnotationPropertyEnum annotationPropertyEnum;

    public AnnotationProperty(String propertyName, AnnotationPropertyEnum annotationPropertyEnum) {
        this.propertyName = propertyName;
        this.annotationPropertyEnum = annotationPropertyEnum;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public AnnotationPropertyEnum getAnnotationPropertyEnum() {
        return annotationPropertyEnum;
    }

}
