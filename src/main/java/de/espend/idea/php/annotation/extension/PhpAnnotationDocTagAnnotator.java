package de.espend.idea.php.annotation.extension;

import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface PhpAnnotationDocTagAnnotator {
    void annotate(PhpAnnotationDocTagAnnotatorParameter docTagAnnotationAnnotatorParameter);
}
