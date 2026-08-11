package de.espend.idea.php.annotation.tests.inspection

import de.espend.idea.php.annotation.inspection.AnnotationDeprecatedInspection
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

class AnnotationDeprecatedInspectionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes_deprecated.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/inspection/fixtures"
    }

    fun testThatInspectionIsDisplayedForAnnotationClasses() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\E<caret>ntity()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDeprecatedInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Foo<caret>bar()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDeprecatedInspection.MESSAGE,
        )
    }
}
