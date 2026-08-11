package de.espend.idea.php.annotation.tests

import de.espend.idea.php.annotation.Settings

class SettingsTest : AnnotationLightCodeInsightFixtureTestCase() {
    fun testProjectServiceCanBeCreated() {
        val settings = Settings.getInstance(project)

        assertSame(settings, Settings.getInstance(project))
        assertSame(settings, settings.state)
    }
}
