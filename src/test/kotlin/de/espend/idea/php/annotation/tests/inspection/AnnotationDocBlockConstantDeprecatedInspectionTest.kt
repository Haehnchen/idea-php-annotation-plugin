package de.espend.idea.php.annotation.tests.inspection

import de.espend.idea.php.annotation.inspection.AnnotationDocBlockConstantDeprecatedInspection
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @see AnnotationDocBlockConstantDeprecatedInspection
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationDocBlockConstantDeprecatedInspectionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/inspection/fixtures"
    }

    fun testTheClassConstantProvidesNotificationForDeprecatedClassUsage() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Foobar\Bar\FooBarDeprecated;
                /**
                 * @Foobar(type=FooBarDeprecated::cl<caret>ass)
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foobar\Bar\FooBar;
                /**
                 * @Foobar(type=FooBarDeprecated::cl<caret>ass)
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE,
        )
    }

    fun testTheConstantProvidesNotificationForDeprecatedUsage() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php
                use Foobar\Bar\FooBarDeprecated;
                /**
                 * @Foobar(type=FooBarDeprecated::I_AM_DEP<caret>RECATED)
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foobar\Bar\FooBarDeprecated;
                /**
                 * @Foobar(type=FooBarDeprecated::I_AM_NOT_DEPRECATED)
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE,
        )
    }
}
