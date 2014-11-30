package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationPropertyParameter {

    final private PsiElement element;
    final private PhpClass phpClass;
    final private Type type;

    final private Project project;
    private String propertyName;

    public AnnotationPropertyParameter(@NotNull PsiElement element, @NotNull PhpClass phpClass, @NonNls String propertyName, @NotNull Type type) {
        this(element, phpClass, type);
        this.propertyName = propertyName;
    }

    public AnnotationPropertyParameter(@NotNull PsiElement psiElement, @NotNull PhpClass phpClass, @NotNull Type type) {
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
    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public PsiElement getElement() {
        return element;
    }

    @NotNull
    public PhpClass getPhpClass() {
        return phpClass;
    }

    @Nullable
    public String getPropertyName() {
        return propertyName;
    }

    @NotNull
    public Project getProject() {
        return project;
    }

    public static enum Type {
        DEFAULT, PROPERTY_VALUE
    }

}
