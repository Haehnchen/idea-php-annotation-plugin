package de.espend.idea.php.annotation.completion.insert

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler
import de.espend.idea.php.annotation.ApplicationSettings
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AttributeAliasInsertHandler private constructor() : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, lookupElement: LookupElement) {
        val alias = (lookupElement as? PhpClassAnnotationLookupElement)?.alias
        if (alias != null) {
            if (!insertAliasUse(context, alias)) {
                return
            }
        } else {
            AnnotationTagInsertHandler.preAliasInsertion(context, lookupElement)
            PhpReferenceInsertHandler.getInstance().handleInsert(context, lookupElement)
        }

        if (
            ApplicationSettings.getInstance().appendRoundBracket &&
            !PhpInsertHandlerUtil.isStringAtCaret(context.editor, "(")
        ) {
            PhpInsertHandlerUtil.insertStringAtCaret(context.editor, "()")
            context.editor.caretModel.moveCaretRelatively(-1, 0, false, false, true)
        }
    }

    companion object {
        private val INSTANCE = AttributeAliasInsertHandler()

        fun getInstance(): AttributeAliasInsertHandler = INSTANCE
    }
}
