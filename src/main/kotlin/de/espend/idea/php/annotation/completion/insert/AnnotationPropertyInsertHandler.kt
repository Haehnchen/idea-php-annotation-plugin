package de.espend.idea.php.annotation.completion.insert

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil
import de.espend.idea.php.annotation.dict.AnnotationProperty
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationPropertyInsertHandler private constructor() : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, lookupElement: LookupElement) {
        // value completion should not fire when already presented:
        // eng| = "value"
        // eng|="value"
        if (
            PhpInsertHandlerUtil.isStringAtCaret(context.editor, "=") ||
            PhpInsertHandlerUtil.isStringAtCaret(context.editor, " =")
        ) {
            return
        }

        val property = lookupElement.`object` as? AnnotationProperty

        // append completion text depend on value:
        // engine="|"
        // engine={|}
        // engine=<boolean|integer>
        val textToInsert = when (property?.annotationPropertyEnum) {
            AnnotationPropertyEnum.ARRAY -> "={}"
            AnnotationPropertyEnum.INTEGER,
            AnnotationPropertyEnum.BOOLEAN,
            -> "="

            else -> "=\"\""
        }

        PhpInsertHandlerUtil.insertStringAtCaret(context.editor, textToInsert)

        // move caret back
        if (property?.annotationPropertyEnum != AnnotationPropertyEnum.INTEGER &&
            property?.annotationPropertyEnum != AnnotationPropertyEnum.BOOLEAN
        ) {
            context.editor.caretModel.moveCaretRelatively(-1, 0, false, false, true)
        }
    }

    companion object {
        private val INSTANCE = AnnotationPropertyInsertHandler()

        fun getInstance(): AnnotationPropertyInsertHandler = INSTANCE
    }
}
