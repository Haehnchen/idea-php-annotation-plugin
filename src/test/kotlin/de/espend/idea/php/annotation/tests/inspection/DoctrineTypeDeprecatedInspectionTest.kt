package de.espend.idea.php.annotation.tests.inspection

import de.espend.idea.php.annotation.doctrine.inspection.DoctrineTypeDeprecatedInspection
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see DoctrineTypeDeprecatedInspection
 */
class DoctrineTypeDeprecatedInspectionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("DoctrineTypeDeprecatedInspection.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/inspection/fixtures"
    }

    fun testThatDeprecatedInspectionIsDisplayedDoctrineColumnTypes() {
        assertLocalInspectionContains(
            "test.php",
            """<?php
                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Column(type="json<caret>_array")
                 */
                class Foo
                {
                }
            """.trimIndent(),
            "[Annotations] Deprecated: Use JsonType instead",
        )

        assertLocalInspectionIsEmpty(
            "test.php",
            """<?php
                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Column(type="json<caret>")
                 */
                class Foo
                {
                }
            """.trimIndent(),
        )
    }

    fun testThatDeprecatedInspectionIsDisplayedDoctrineColumnTypesForAttribute() {
        assertLocalInspectionContains(
            "test.php",
            """<?php
                use Doctrine\ORM\Mapping as ORM;

                #[ORM\Column(type: 'json<caret>_array')]
                class Foo
                {
                }
            """.trimIndent(),
            "[Annotations] Deprecated: Use JsonType instead",
        )

        assertLocalInspectionIsEmpty(
            "test.php",
            """<?php
                use Doctrine\ORM\Mapping as ORM;

                #[ORM\Foobar(type: 'json<caret>_array')]
                class Foo
                {
                }
            """.trimIndent(),
        )
    }
}
