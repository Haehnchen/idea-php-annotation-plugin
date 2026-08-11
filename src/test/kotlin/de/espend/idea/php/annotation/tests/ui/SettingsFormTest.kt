package de.espend.idea.php.annotation.tests.ui

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase
import de.espend.idea.php.annotation.ui.SettingsForm

class SettingsFormTest : AnnotationLightCodeInsightFixtureTestCase() {
    fun testCanBeCreated() {
        val configurable = SettingsForm()

        assertNotNull(configurable.createComponent())
        assertFalse(configurable.isModified)
    }
}
