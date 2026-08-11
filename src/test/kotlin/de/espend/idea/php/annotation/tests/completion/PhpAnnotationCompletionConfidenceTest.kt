package de.espend.idea.php.annotation.tests.completion

import com.intellij.util.ThreeState
import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.completion.PhpAnnotationCompletionConfidence
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

class PhpAnnotationCompletionConfidenceTest : AnnotationLightCodeInsightFixtureTestCase() {
    private val confidence = PhpAnnotationCompletionConfidence()

    fun testAllowsAutopopupInsideAnnotationString() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                /** @Route(name="fo<caret>o") */
                class Controller {}
            """.trimIndent(),
        )

        assertEquals(ThreeState.NO, shouldSkipAutopopup())
    }

    fun testKeepsDefaultConfidenceInsidePhpString() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                ${'$'}value = "fo<caret>o";
            """.trimIndent(),
        )

        assertEquals(ThreeState.UNSURE, shouldSkipAutopopup())
    }

    private fun shouldSkipAutopopup(): ThreeState {
        val contextElement = myFixture.file.findElementAt(myFixture.caretOffset)
            ?: myFixture.file.findElementAt(myFixture.caretOffset - 1)
            ?: error("No PSI element at caret")

        return confidence.shouldSkipAutopopup(
            editor,
            contextElement,
            myFixture.file,
            myFixture.caretOffset,
        )
    }
}
