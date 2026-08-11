package de.espend.idea.php.annotation.tests.inspection

import de.espend.idea.php.annotation.inspection.AnnotationMissingUseInspection
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see AnnotationMissingUseInspection
 */
class AnnotationMissingUseInspectionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/inspection/fixtures"
    }

    fun testThatInspectionIsDisplayedForAnnotationClasses() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php


                /**
                 * @E<caret>ntity()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationMissingUseInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foo\Entity;

                /**
                 * @E<caret>ntity()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationMissingUseInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php

                /**
                 * @\E<caret>ntity()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationMissingUseInspection.MESSAGE,
        )
    }

    fun testThatInspectionIsNotDisplayedForClassesWhichDoesNotHaveAValidImportPath() {
        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foo\Entity;

                /**
                 * @Entity()
                 * @Foo()
                 * @Fo<caret>obar()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationMissingUseInspection.MESSAGE,
        )
    }

    fun testThatInspectionIsDisplayedForAnnotationClassesWithAlias() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php


                /**
                 * @ORM\E<caret>ntity()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationMissingUseInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php
                use Foo\Bar as ORM;

                /**
                 * @ORM\E<caret>ntity()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationMissingUseInspection.MESSAGE,
        )
    }

    fun testThatBlacklistedAnnotationDoesNotProvideInpsectionMessage() {
        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php


                /**
                 * @Annot<caret>ation()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationMissingUseInspection.MESSAGE,
        )

        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php


                /**
                 * @noinspe<caret>ction()
                 */
                class Foo
                {
                }
            """.trimIndent(),
            AnnotationMissingUseInspection.MESSAGE,
        )
    }
}
