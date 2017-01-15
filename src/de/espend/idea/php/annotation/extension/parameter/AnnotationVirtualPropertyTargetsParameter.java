package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationVirtualPropertyTargetsParameter {
    @NotNull
    private final PhpClass phpClass;

    @NotNull
    private final PsiElement psiElement;

    @NotNull
    private final String property;

    @NotNull
    private final Collection<PsiElement> targets = new ArrayList<>();

    public AnnotationVirtualPropertyTargetsParameter(@NotNull PhpClass phpClass, @NotNull PsiElement psiElement, @NotNull String property) {
        this.phpClass = phpClass;
        this.psiElement = psiElement;
        this.property = property;
    }

    @NotNull
    public PhpClass getPhpClass() {
        return phpClass;
    }

    @NotNull
    public PsiElement getPsiElement() {
        return psiElement;
    }

    @NotNull
    public String getProperty() {
        return property;
    }

    public void addTarget(@NotNull PsiElement psiElement) {
        targets.add(psiElement);
    }

    @NotNull
    public Collection<PsiElement> getTargets() {
        return targets;
    }
}
