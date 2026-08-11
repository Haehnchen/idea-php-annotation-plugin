package de.espend.idea.php.annotation.completion.lookupelements

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.dict.UseAliasOption

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class PhpClassAnnotationLookupElement : LookupElement {
    private val phpClass: PhpClass
    private val lookupString: String
    var alias: UseAliasOption? = null
        private set
    private var insertHandler: InsertHandler<LookupElement>? = null
    private var typeText: String? = null

    constructor(phpClass: PhpClass) {
        this.phpClass = phpClass
        this.lookupString = phpClass.name
    }

    constructor(phpClass: PhpClass, alias: UseAliasOption?, lookupString: String) {
        this.phpClass = phpClass
        this.alias = alias
        this.lookupString = lookupString
    }

    fun withInsertHandler(insertHandler: InsertHandler<LookupElement>): PhpClassAnnotationLookupElement {
        this.insertHandler = insertHandler
        return this
    }

    override fun getLookupString(): String = lookupString

    override fun renderElement(presentation: LookupElementPresentation) {
        presentation.itemText = lookupString
        presentation.typeText = typeText ?: phpClass.presentableFQN
        presentation.icon = phpClass.icon
        presentation.isStrikeout = phpClass.isDeprecated
    }

    override fun handleInsert(context: InsertionContext) {
        insertHandler?.handleInsert(context, this)
    }

    fun withTypeText(typeText: String): PhpClassAnnotationLookupElement {
        this.typeText = typeText
        return this
    }

    override fun getObject(): Any = phpClass
}
