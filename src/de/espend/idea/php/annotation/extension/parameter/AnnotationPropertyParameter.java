package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

public class AnnotationPropertyParameter {

    final private PsiElement element;
    final private PhpClass phpClass;
    final private Type type;

    final private Project project;
    private String propertyName;

    public AnnotationPropertyParameter(PsiElement element, PhpClass phpClass, String propertyName, Type type) {
        this(element, phpClass, type);
        this.propertyName = propertyName;
    }

    public AnnotationPropertyParameter(PsiElement psiElement, PhpClass phpClass, Type type) {
        this.element = psiElement;
        this.phpClass = phpClass;
        this.type = type;
        this.project = phpClass.getProject();
    }

    /**
     * DEFAULT: '@Template("foo.twig.html")'
     * PROPERTY_VALUE: '@Service("foo")'
     *
     * @return Value type
     */
    public Type getType() {
        return type;
    }

    public PsiElement getElement() {
        return element;
    }

    public PhpClass getPhpClass() {
        return phpClass;
    }

    @Nullable
    public String getPropertyName() {
        return propertyName;
    }

    public Project getProject() {
        return project;
    }

    public static enum Type {
        DEFAULT, PROPERTY_VALUE
    }

}
