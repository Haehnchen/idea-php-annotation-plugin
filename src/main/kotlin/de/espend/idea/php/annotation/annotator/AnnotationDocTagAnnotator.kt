package de.espend.idea.php.annotation.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter
import de.espend.idea.php.annotation.util.AnnotationUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
open class AnnotationDocTagAnnotator : Annotator {
    override fun annotate(psiElement: PsiElement, holder: AnnotationHolder) {
        if (psiElement !is PhpDocTag) {
            return
        }

        val name = psiElement.name
        if (AnnotationUtil.isBlockedAnnotationTag(name)) {
            return
        }

        if (!AnnotationUtil.isAnnotationPhpDocTag(psiElement)) {
            return
        }

        val phpClass = AnnotationUtil.getAnnotationReference(psiElement)
        if (phpClass == null) {
            val parameter = PhpAnnotationDocTagAnnotatorParameter(psiElement, holder)
            for (annotator in AnnotationUtil.EP_DOC_TAG_ANNOTATOR.extensions) {
                annotator.annotate(parameter)
            }

            return
        }

        val parameter = PhpAnnotationDocTagAnnotatorParameter(phpClass, psiElement, holder)
        for (annotator in AnnotationUtil.EP_DOC_TAG_ANNOTATOR.extensions) {
            annotator.annotate(parameter)
        }
    }
}
