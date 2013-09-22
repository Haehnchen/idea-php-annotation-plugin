package de.espend.idea.php.annotation.dict;

import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhpAnnotation {

    private PhpClass phpClass;
    private List<AnnotationTarget> targets = new ArrayList<AnnotationTarget>();

    public PhpAnnotation(PhpClass phpClass, List<AnnotationTarget> annotationTargets) {
        this.phpClass = phpClass;
        this.targets = annotationTargets;
    }

    public PhpAnnotation(PhpClass phpClass, AnnotationTarget annotationTargets) {
        this(phpClass, Arrays.asList(annotationTargets));
    }

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

    public List<AnnotationTarget> getTargets() {
        return targets;
    }

}
