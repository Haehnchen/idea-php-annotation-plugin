package de.espend.idea.php.annotation.tests.util.ide

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase
import de.espend.idea.php.annotation.util.ide.PluginErrorReporterSubmitter

class PluginErrorReporterSubmitterTest : AnnotationLightCodeInsightFixtureTestCase() {
    fun testCanBeCreated() {
        val submitter = PluginErrorReporterSubmitter()

        assertEquals("Report to espend.de", submitter.reportActionText)
    }
}
