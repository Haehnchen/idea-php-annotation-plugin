package de.espend.idea.php.annotation.tests

import de.espend.idea.php.annotation.ApplicationSettings
import de.espend.idea.php.annotation.dict.UseAliasOption

class ApplicationSettingsTest : AnnotationLightCodeInsightFixtureTestCase() {
    fun testApplicationServiceCanBeCreated() {
        val settings = ApplicationSettings.getInstance()

        assertSame(settings, ApplicationSettings.getInstance())
        assertSame(settings, settings.state)
    }

    fun testLoadStateCopiesValues() {
        val settings = ApplicationSettings()
        val state = ApplicationSettings().apply {
            appendRoundBracket = false
            useAliasOptions = arrayListOf(UseAliasOption("App\\Annotation", "App", false))
            provideDefaults = false
        }

        settings.loadState(state)

        assertFalse(settings.appendRoundBracket)
        assertFalse(settings.provideDefaults)
        assertEquals(1, settings.useAliasOptions.size)
        assertEquals("App\\Annotation", settings.useAliasOptions.single().className)
        assertEquals("App", settings.useAliasOptions.single().alias)
        assertFalse(settings.useAliasOptions.single().isEnabled)
    }

    fun testProvidesBuiltInAliases() {
        val option = ApplicationSettings.getDefaultUseAliasOption().firstOrNull {
            it.className == "Doctrine\\ORM\\Mapping"
        }

        assertNotNull(option)
        assertEquals("ORM", option!!.alias)
        assertTrue(option.isEnabled)
    }
}
