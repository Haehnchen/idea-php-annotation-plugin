package de.espend.idea.php.annotation.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.Collection;
import java.util.List;

public class AnnotationDocTagGotoHandlerParameter {

    final private PhpDocTag phpDocTag;
    final private List<PsiElement> targets;
    final private PhpClass phpClass;

    public AnnotationDocTagGotoHandlerParameter(PhpDocTag phpDocTag, PhpClass phpClass, List<PsiElement> targets) {
        this.phpDocTag = phpDocTag;
        this.targets = targets;
        this.phpClass = phpClass;
    }

    public Project getProject() {
        return phpDocTag.getProject();
    }

    public void addTarget(PsiElement psiElement) {
        this.targets.add(psiElement);
    }

    public void addTargets(Collection<PsiElement> psiElements) {
        this.targets.addAll(psiElements);
    }

    public List<PsiElement> getTargets() {
        return targets;
    }

    public PhpClass getPhpClass() {
        return phpClass;
    }

}
