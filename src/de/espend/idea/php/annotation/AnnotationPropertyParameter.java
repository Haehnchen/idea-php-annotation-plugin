package de.espend.idea.php.annotation;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

public class AnnotationPropertyParameter {

    private PsiElement element;
    private PhpClass phpClass;
    private String propertyName;
    private Type type;

    public AnnotationPropertyParameter(PsiElement element, PhpClass phpClass, String propertyName, Type type) {
        this(element, phpClass, type);
        this.propertyName = propertyName;
    }

    public AnnotationPropertyParameter(PsiElement psiElement, PhpClass phpClass, Type type) {
        this.element = psiElement;
        this.phpClass = phpClass;
        this.type = type;
    }

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

    public static enum Type {
        DEFAULT, STRING, ARRAY
    }

}
