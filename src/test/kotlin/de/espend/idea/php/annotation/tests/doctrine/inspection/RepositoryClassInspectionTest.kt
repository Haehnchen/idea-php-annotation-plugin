package de.espend.idea.php.annotation.tests.doctrine.inspection

import de.espend.idea.php.annotation.doctrine.inspection.RepositoryClassInspection
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see RepositoryClassInspection
 */
class RepositoryClassInspectionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/inspection/fixtures"
    }

    fun testThatInspectionForMissingClassIsProvided() {
        assertLocalInspectionContains(
            "test.php",
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Entity(repositoryClass="Foo<caret>bar")
                 */
                class Foo
                {
                }
            """.trimIndent(),
            RepositoryClassInspection.MESSAGE,
        )

        assertLocalInspectionContains(
            "test.php",
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Entity("Foobar", repositoryClass="Foo<caret>bar")
                 */
                class Foo
                {
                }
            """.trimIndent(),
            RepositoryClassInspection.MESSAGE,
        )
    }

    fun testThatExistingClassIsNotHighlighted() {
        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Entity(repositoryClass="Foob<caret>ar\Foo")
                 */
                class Foo
                {
                }
            """.trimIndent(),
            RepositoryClassInspection.MESSAGE,
        )
    }

    fun testThatExistingClassInSameNamespaceIsNotHighlighted() {
        assertLocalInspectionContainsNotContains(
            "test.php",
            """
                <?php

                namespace Foobar;use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Entity(repositoryClass="F<caret>oo")
                 */
                class Bar
                {
                }
            """.trimIndent(),
            RepositoryClassInspection.MESSAGE,
        )
    }
}
