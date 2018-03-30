package de.espend.idea.php.annotation.dict;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpAnnotation {

    @NotNull
    final private PhpClass phpClass;

    @NotNull
    final private List<AnnotationTarget> targets;

    public PhpAnnotation(@NotNull PhpClass phpClass, @NotNull List<AnnotationTarget> annotationTargets) {
        this.phpClass = phpClass;
        this.targets = annotationTargets;
    }

    public PhpAnnotation(PhpClass phpClass, AnnotationTarget annotationTargets) {
        this(phpClass, Collections.singletonList(annotationTargets));
    }

    @NotNull
    public PhpClass getPhpClass() {
        return phpClass;
    }

    public boolean hasTarget(AnnotationTarget... hasTargets) {

        for(AnnotationTarget annotationTarget: hasTargets) {
            if(this.getTargets().contains(annotationTarget)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    public List<AnnotationTarget> getTargets() {
        return targets;
    }

    public boolean matchOneOf(AnnotationTarget... matchOneTargets) {
        for(AnnotationTarget matchOneTarget: matchOneTargets) {
            if(this.targets.contains(matchOneTarget)) {
                return true;
            }
        }

        return false;
    }
}
