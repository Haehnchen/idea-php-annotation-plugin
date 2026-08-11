package de.espend.idea.php.annotation.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import de.espend.idea.php.annotation.inspection.visitor.PhpDocTagWithUsePsiElementVisitor
import de.espend.idea.php.annotation.util.PhpDocUtil
import de.espend.idea.php.annotation.util.PhpElementsUtil

/**
 * Attach constant deprecated also includes "::class"
 *
 * - "@FOO(test=Test::cl<caret>ass)"
 * - "@FOO(test=Test::VERS<caret>ION)"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
open class AnnotationDocBlockConstantDeprecatedInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return PhpDocTagWithUsePsiElementVisitor(holder, ::visitAnnotationDocTag)
    }

    private fun visitAnnotationDocTag(
        phpDocTag: PhpDocTag,
        holder: ProblemsHolder,
        lazyUseImporterCollector: AnnotationInspectionUtil.LazyNamespaceImportResolver,
    ) {
        for (element in PsiTreeUtil.collectElements(phpDocTag) {
            it.node.elementType === PhpDocTokenTypes.DOC_STATIC
        }) {
            val nextSibling = element.nextSibling
            if (nextSibling == null || nextSibling.node.elementType !== PhpDocTokenTypes.DOC_IDENTIFIER) {
                continue
            }

            val prevSibling = element.prevSibling ?: return
            val namespaceForDocIdentifier = PhpDocUtil.getNamespaceForDocIdentifier(prevSibling) ?: return
            val clazz = AnnotationInspectionUtil.getClassFqnString(
                namespaceForDocIdentifier,
                lazyUseImporterCollector,
            ) ?: return
            val phpClass = PhpElementsUtil.getClassInterface(phpDocTag.project, clazz) ?: return

            // ::class direct class access
            val text = nextSibling.text
            if (text == "class") {
                if (phpClass.isDeprecated) {
                    holder.registerProblem(
                        nextSibling,
                        MESSAGE,
                        ProblemHighlightType.LIKE_DEPRECATED,
                    )
                }

                return
            }

            // ::CONST fetch the field
            val fieldByName = phpClass.findFieldByName(text, true)
            if (fieldByName != null && fieldByName.isConstant && fieldByName.isDeprecated) {
                holder.registerProblem(
                    nextSibling,
                    MESSAGE,
                    ProblemHighlightType.LIKE_DEPRECATED,
                )
            }
        }
    }

    override fun runForWholeFile(): Boolean {
        return true
    }

    companion object {
        const val MESSAGE = "[Annotations] Deprecated usage"
    }
}
