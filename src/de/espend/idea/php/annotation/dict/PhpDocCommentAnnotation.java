package de.espend.idea.php.annotation.dict;

import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PhpDocCommentAnnotation {

    final private Map<String, PhpDocTagAnnotation> annotationReferences;
    final private PhpDocComment phpDocComment;

    public PhpDocCommentAnnotation(Map<String, PhpDocTagAnnotation> annotationReferences, PhpDocComment phpDocComment) {
        this.annotationReferences = annotationReferences;
        this.phpDocComment = phpDocComment;
    }

    @Nullable
    public PhpDocTagAnnotation getPhpDocBlock(String className) {
        if(className.startsWith("\\")) className = className.substring(1);
        return annotationReferences.containsKey(className) ? annotationReferences.get(className) : null;
    }

    @Nullable
    public PhpDocTagAnnotation getFirstPhpDocBlock(String... classNames) {
        for(String className: classNames) {
            PhpDocTagAnnotation phpDocTagAnnotation = getPhpDocBlock(className);
            if(phpDocTagAnnotation != null) {
                return phpDocTagAnnotation;
            }
        }

        return null;
    }

    @NotNull
    public PhpDocComment getPhpDocComment() {
        return phpDocComment;
    }

    @NotNull
    public Map<String, PhpDocTagAnnotation> getAnnotationReferences() {
        return annotationReferences;
    }

}
