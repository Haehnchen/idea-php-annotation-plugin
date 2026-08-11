package de.espend.idea.php.annotation.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import de.espend.idea.php.annotation.util.AnnotationUtil

open class AnnotationDeprecatedInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is PhpDocTag && AnnotationUtil.isAnnotationPhpDocTag(element)) {
                    visitAnnotationDocTag(element, holder)
                }

                super.visitElement(element)
            }
        }
    }

    private fun visitAnnotationDocTag(phpDocTag: PhpDocTag, holder: ProblemsHolder) {
        val phpClass = AnnotationUtil.getAnnotationReference(phpDocTag)
        if (phpClass == null || !phpClass.isDeprecated) {
            return
        }

        val firstChild = phpDocTag.firstChild
        if (firstChild == null || firstChild.node.elementType !== PhpDocElementTypes.DOC_TAG_NAME) {
            return
        }

        holder.registerProblem(firstChild, MESSAGE, ProblemHighlightType.LIKE_DEPRECATED)
    }

    override fun runForWholeFile(): Boolean {
        return true
    }

    companion object {
        const val MESSAGE = "[Annotations] Annotation is deprecated"
    }
}
