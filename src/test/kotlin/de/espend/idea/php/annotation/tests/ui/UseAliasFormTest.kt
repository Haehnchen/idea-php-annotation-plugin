package de.espend.idea.php.annotation.tests.ui

import com.intellij.openapi.ui.DialogWrapper
import de.espend.idea.php.annotation.dict.UseAliasOption
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase
import de.espend.idea.php.annotation.ui.UseAliasForm

class UseAliasFormTest : AnnotationLightCodeInsightFixtureTestCase() {
    fun testCanBeCreated() {
        val form = UseAliasForm(null, UseAliasOption("Doctrine\\ORM\\Mapping", "ORM", true)) { }

        assertNotNull(form.preferredFocusedComponent)
        form.close(DialogWrapper.CANCEL_EXIT_CODE)
    }
}
