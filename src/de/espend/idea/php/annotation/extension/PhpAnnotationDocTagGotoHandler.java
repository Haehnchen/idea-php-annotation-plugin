package de.espend.idea.php.annotation.extension;

import de.espend.idea.php.annotation.extension.parameter.AnnotationDocTagGotoHandlerParameter;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface PhpAnnotationDocTagGotoHandler {
    void getGotoDeclarationTargets(AnnotationDocTagGotoHandlerParameter parameter);
}
