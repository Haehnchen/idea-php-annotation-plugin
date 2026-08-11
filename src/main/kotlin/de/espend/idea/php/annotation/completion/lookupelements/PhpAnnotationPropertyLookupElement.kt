package de.espend.idea.php.annotation.completion.lookupelements

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.jetbrains.php.PhpIcons
import de.espend.idea.php.annotation.completion.insert.AnnotationPropertyInsertHandler
import de.espend.idea.php.annotation.dict.AnnotationProperty

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class PhpAnnotationPropertyLookupElement(
    private val annotationProperty: AnnotationProperty,
) : LookupElement() {
    override fun getLookupString(): String = annotationProperty.propertyName

    override fun renderElement(presentation: LookupElementPresentation) {
        presentation.itemText = lookupString
        presentation.typeText = annotationProperty.annotationPropertyEnum.name
            .lowercase()
            .replaceFirstChar { character -> character.uppercase() }
        presentation.isTypeGrayed = true
        presentation.icon = PhpIcons.FIELD
    }

    override fun handleInsert(context: InsertionContext) {
        AnnotationPropertyInsertHandler.getInstance().handleInsert(context, this)
    }

    override fun getObject(): Any = annotationProperty
}
