package de.espend.idea.php.annotation.completion.insert

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.containers.ContainerUtil
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.ApplicationSettings
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement
import de.espend.idea.php.annotation.dict.UseAliasOption
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpElementsUtil
import org.apache.commons.lang3.StringUtils

internal fun insertAliasUse(context: InsertionContext, alias: UseAliasOption): Boolean {
    val element = PsiUtilCore.getElementAtOffset(context.file, context.startOffset)
    val scope = PhpCodeInsightUtil.findScopeForUseOperator(element) ?: return false
    val className = "\\" + StringUtils.stripStart(alias.className, "\\")
    PhpElementsUtil.insertUseIfNecessary(scope, className, alias.alias)
    PsiDocumentManager.getInstance(context.project)
        .doPostponedOperationsAndUnblockDocument(context.document)
    return true
}

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationTagInsertHandler private constructor() : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, lookupElement: LookupElement) {
        val alias = (lookupElement as? PhpClassAnnotationLookupElement)?.alias

        // "ORM\Entity"
        if (alias != null) {
            if (!insertAliasUse(context, alias)) {
                return
            }
        } else {
            // find alias in settings "\Foo\Bar as Car" for given PhpClass insertion context
            preAliasInsertion(context, lookupElement)

            // reuse jetbrains "use importer": this is private only so we need some workaround
            // to not implement your own algo for that
            PhpReferenceInsertHandler.getInstance().handleInsert(context, lookupElement)
        }

        // force "@Foo" => "@Foo(<caret>)"
        if (
            ApplicationSettings.getInstance().appendRoundBracket &&
            !PhpInsertHandlerUtil.isStringAtCaret(context.editor, "(")
        ) {
            PhpInsertHandlerUtil.insertStringAtCaret(context.editor, "()")
            context.editor.caretModel.moveCaretRelatively(-1, 0, false, false, true)
        }

        // "@" is not provide by lookupelements element because its remove by auto import so attach it if necessary
        val element = PsiUtilCore.getElementAtOffset(context.file, context.startOffset)
        if (!element.text.startsWith("@")) {
            context.document.insertString(context.startOffset, "@")
        }
    }

    companion object {
        private val INSTANCE = AnnotationTagInsertHandler()

        fun getInstance(): AnnotationTagInsertHandler = INSTANCE

        /**
         * Insert class alias before PhpStorm tries to import a new use statement "\Foo\Bar as Car"
         */
        fun preAliasInsertion(context: InsertionContext, lookupElement: LookupElement) {
            val importsAliases = AnnotationUtil.getActiveImportsAliasesFromSettings()
            if (importsAliases.isEmpty()) {
                return
            }

            val phpClass = lookupElement.`object` as? PhpClass ?: return
            val fqn = StringUtils.stripStart(phpClass.fqn, "\\")
            val option = ContainerUtil.find(importsAliases) { candidate: UseAliasOption ->
                candidate.alias != null &&
                    candidate.className != null &&
                    fqn.startsWith(StringUtils.stripStart(candidate.className, "\\"))
            }
            val classNameSetting = option?.className ?: return
            val alias = option.alias ?: return
            val element = context.file.findElementAt(context.editor.caretModel.offset) ?: return
            val scope = PhpCodeInsightUtil.findScopeForUseOperator(element) ?: return
            val className = if (classNameSetting.startsWith("\\")) classNameSetting else "\\$classNameSetting"

            PhpElementsUtil.insertUseIfNecessary(scope, className, alias)
            PsiDocumentManager.getInstance(context.project)
                .doPostponedOperationsAndUnblockDocument(context.document)
        }
    }
}
