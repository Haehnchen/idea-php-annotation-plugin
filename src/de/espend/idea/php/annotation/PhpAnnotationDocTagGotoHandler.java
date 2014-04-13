package de.espend.idea.php.annotation;

import de.espend.idea.php.annotation.navigation.AnnotationDocTagGotoHandlerParameter;

public interface PhpAnnotationDocTagGotoHandler {
    public void getGotoDeclarationTargets(AnnotationDocTagGotoHandlerParameter parameter);
}
