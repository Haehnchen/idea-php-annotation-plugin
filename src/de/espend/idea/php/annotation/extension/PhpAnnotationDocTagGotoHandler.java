package de.espend.idea.php.annotation.extension;

import de.espend.idea.php.annotation.extension.parameter.AnnotationDocTagGotoHandlerParameter;

public interface PhpAnnotationDocTagGotoHandler {
    public void getGotoDeclarationTargets(AnnotationDocTagGotoHandlerParameter parameter);
}
