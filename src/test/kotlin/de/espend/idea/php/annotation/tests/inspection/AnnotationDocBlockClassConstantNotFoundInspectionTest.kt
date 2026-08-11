package de.espend.idea.php.annotation.tests.inspection

import de.espend.idea.php.annotation.inspection.AnnotationDocBlockClassConstantNotFoundInspection
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @see AnnotationDocBlockClassConstantNotFoundInspection
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationDocBlockClassConstantNotFoundInspectionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/inspection/fixtures"
    }

    fun testThatClassConstantProvideMissingUseHighlight() {
        assertLocalInspectionContains(
            "test.php",
            """<?php
                use Foo;
                /**
                 * @Foobar(type=Foo\Unknown::cl<caret>ass)
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """<?php
                /**
                 * @Foobar(type=\Foobar\Bar\FooBar::cl<caret>ass)
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """<?php
                use Foobar\Bar\FooBar;
                /**
                 * @Foobar(type=FooBar::cl<caret>ass)
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """<?php
                use Foobar\Bar;
                /**
                 * @Foobar(type=FooBar\FooBar::cl<caret>ass)
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE,
        )
    }
}
