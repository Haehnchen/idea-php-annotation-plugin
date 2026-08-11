package de.espend.idea.php.annotation.tests.ui

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase
import de.espend.idea.php.annotation.ui.UseAliasListForm

class UseAliasListFormTest : AnnotationLightCodeInsightFixtureTestCase() {
    fun testCanBeCreated() {
        val configurable = UseAliasListForm()

        assertNotNull(configurable.createComponent())
        assertFalse(configurable.isModified)
    }
}
