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
 * Provide inpsection check for the class of "foo=Foo\Foo::cla<caret>ss"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
open class AnnotationDocBlockClassConstantNotFoundInspection : LocalInspectionTool() {
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
            if (
                nextSibling == null ||
                nextSibling.node.elementType !== PhpDocTokenTypes.DOC_IDENTIFIER ||
                nextSibling.text != "class"
            ) {
                continue
            }

            val prevSibling = element.prevSibling ?: return
            val namespaceForDocIdentifier = PhpDocUtil.getNamespaceForDocIdentifier(prevSibling) ?: return
            val clazz = AnnotationInspectionUtil.getClassFqnString(
                namespaceForDocIdentifier,
                lazyUseImporterCollector,
            ) ?: return

            val classInterface = PhpElementsUtil.getClassInterface(phpDocTag.project, clazz)
            if (classInterface == null) {
                holder.registerProblem(
                    nextSibling,
                    MESSAGE,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                )
            }
        }
    }

    override fun runForWholeFile(): Boolean {
        return true
    }

    companion object {
        const val MESSAGE = "[Annotations] Class not found"
    }
}
