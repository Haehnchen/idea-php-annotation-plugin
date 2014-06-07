package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.util.AnnotationUtil;
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

    @Nullable
    public PhpDocTagAnnotation getAnnotationDocTag() {
        return AnnotationUtil.getPhpDocAnnotationContainer(this.phpDocTag);
    }

    @NotNull
    public AnnotationHolder getHolder() {
        return holder;
    }

    @NotNull
    public Project getProject() {
        return this.phpDocTag.getProject();
    }

    @Nullable
    public PhpClass getAnnotationClass() {
        return annotationClass;
    }

}
