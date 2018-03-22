package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocTagGotoHandlerParameter {

    final private PhpDocTag phpDocTag;
    final private List<PsiElement> targets;
    final private PhpClass phpClass;

    public AnnotationDocTagGotoHandlerParameter(@NotNull PhpDocTag phpDocTag, @NotNull PhpClass phpClass, @NotNull List<PsiElement> targets) {
        this.phpDocTag = phpDocTag;
        this.targets = targets;
        this.phpClass = phpClass;
    }

    @NotNull
    public Project getProject() {
        return phpDocTag.getProject();
    }

    public void addTarget(PsiElement psiElement) {
        this.targets.add(psiElement);
    }

    public void addTargets(Collection<PsiElement> psiElements) {
        this.targets.addAll(psiElements);
    }

    @NotNull
    public List<PsiElement> getTargets() {
        return targets;
    }

    @NotNull
    public PhpClass getPhpClass() {
        return phpClass;
    }

    @NotNull
    public PhpDocTag getPhpDocTag() {
        return phpDocTag;
    }

    @Nullable
    public PhpDocTagAnnotation getAnnotationDocTag() {
        return AnnotationUtil.getPhpDocAnnotationContainer(this.phpDocTag);
    }

}
