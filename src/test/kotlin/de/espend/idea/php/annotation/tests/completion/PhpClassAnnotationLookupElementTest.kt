package de.espend.idea.php.annotation.tests.completion

import com.intellij.codeInsight.lookup.LookupElementPresentation
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase
import de.espend.idea.php.annotation.util.PhpElementsUtil

class PhpClassAnnotationLookupElementTest : AnnotationLightCodeInsightFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/completion/fixtures"

    fun testCustomTypeTextIsRendered() {
        val phpClass = PhpElementsUtil.getClassInterface(project, "\\My\\Annotations\\All")
        assertNotNull(phpClass)

        val presentation = LookupElementPresentation()
        PhpClassAnnotationLookupElement(phpClass!!)
            .withTypeText("Imported annotation")
            .renderElement(presentation)

        assertEquals("Imported annotation", presentation.typeText)
    }
}
