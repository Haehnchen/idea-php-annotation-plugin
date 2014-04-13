package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhpAnnotationDocTagAnnotatorParameter {

    private final PhpDocTag phpDocTag;
    private final AnnotationHolder holder;

    @Nullable
    private PhpClass annotationClass;

    public PhpAnnotationDocTagAnnotatorParameter(@NotNull PhpDocTag phpDocTag, @NotNull AnnotationHolder holder) {
        this.phpDocTag = phpDocTag;
        this.holder = holder;
    }

    public PhpAnnotationDocTagAnnotatorParameter(@Nullable PhpClass annotationClass, @NotNull PhpDocTag phpDocTag, @NotNull AnnotationHolder holder) {
        this(phpDocTag, holder);
        this.annotationClass = annotationClass;
    }

    @NotNull
    public PhpDocTag getPhpDocTag() {
        return phpDocTag;
    }

    public AnnotationHolder getHolder() {
        return holder;
    }

    public Project getProject() {
        return this.phpDocTag.getProject();
    }

    @Nullable
    public PhpClass getAnnotationClass() {
        return annotationClass;
    }

}
