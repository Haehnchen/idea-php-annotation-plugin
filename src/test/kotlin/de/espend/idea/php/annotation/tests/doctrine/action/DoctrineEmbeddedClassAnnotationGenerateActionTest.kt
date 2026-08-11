package de.espend.idea.php.annotation.tests.doctrine.action

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrineEmbeddedClassAnnotationGenerateAction
 */
class DoctrineEmbeddedClassAnnotationGenerateActionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/action/fixtures"
    }

    fun testThatThatEmbeddableClassIsGeneratedForAnnotations() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                /**
                 * @\Doctrine\ORM\Mapping\Entity
                 */
                class Existing {}

                class Foobar
                {
                <caret>}
            """.trimIndent(),
        )

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Embedded.ClassGenerator")

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @\Doctrine\ORM\Mapping\Entity
                 */
                class Existing {}

                /**
                 * @ORM\Embeddable
                 */
                class Foobar
                {
                }
            """.trimIndent(),
        )
    }

    fun testThatThatEmbeddableClassIsGeneratedForAttributes() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                #[Foo]
                class Foobar
                {
                <caret>}
            """.trimIndent(),
        )

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Embedded.ClassGenerator")

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                #[ORM\Embeddable]
                #[Foo]
                class Foobar
                {
                }
            """.trimIndent(),
        )
    }
}
