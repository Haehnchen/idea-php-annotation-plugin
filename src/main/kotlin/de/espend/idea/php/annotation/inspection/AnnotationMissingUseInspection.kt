package de.espend.idea.php.annotation.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import de.espend.idea.php.annotation.inspection.visitor.PhpDocTagWithUsePsiElementVisitor
import de.espend.idea.php.annotation.util.AnnotationUtil
import org.apache.commons.lang3.StringUtils

/**
 * Inspection DocTags and their imports
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
open class AnnotationMissingUseInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return PhpDocTagWithUsePsiElementVisitor(holder, ::visitAnnotationDocTag)
    }

    private fun visitAnnotationDocTag(
        phpDocTag: PhpDocTag,
        holder: ProblemsHolder,
        lazyNamespaceImportResolver: AnnotationInspectionUtil.LazyNamespaceImportResolver,
    ) {
        // Target for our inspection is DocTag name: @Foobar() => Foobar
        // This prevent highlighting the complete DocTag
        val firstChild = phpDocTag.firstChild
        if (firstChild == null || firstChild.node.elementType !== PhpDocElementTypes.DOC_TAG_NAME) {
            return
        }

        val name = phpDocTag.name
        val tagName = StringUtils.stripStart(name, "@")

        // ignore "@\Foo" absolute FQN ones
        if (tagName.startsWith("\\")) {
            return
        }

        val split = tagName.split("\\\\".toRegex()).toTypedArray()
        val useImportMap = lazyNamespaceImportResolver.imports
        if (useImportMap.containsKey(split[0])) {
            return
        }

        val annotationReference = AnnotationUtil.getAnnotationReference(phpDocTag)
        if (annotationReference != null) {
            return
        }

        val phpClasses = AnnotationUtil.getPossibleImportClasses(phpDocTag)
        if (phpClasses.isNotEmpty()) {
            val collect = phpClasses.entries.map { Pair.create(it.key, it.value) }

            holder.registerProblem(
                firstChild,
                MESSAGE,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                ImportUseForAnnotationQuickFix(phpDocTag, collect),
            )
        }
    }

    override fun runForWholeFile(): Boolean {
        return true
    }

    companion object {
        const val MESSAGE = "[Annotations] Missing import"
    }
}
