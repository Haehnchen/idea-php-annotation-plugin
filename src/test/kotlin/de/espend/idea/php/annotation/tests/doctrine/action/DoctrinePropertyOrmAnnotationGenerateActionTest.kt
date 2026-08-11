package de.espend.idea.php.annotation.tests.doctrine.action

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrinePropertyOrmAnnotationGenerateAction
 */
class DoctrinePropertyOrmAnnotationGenerateActionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/action/fixtures"
    }

    fun testGeneratesOrmAnnotationForProperty() {
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
                   public ${'$'}id;<caret>
                }
            """.trimIndent(),
        )

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.PropertyGenerator")

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @\Doctrine\ORM\Mapping\Entity
                 */
                class Existing {}

                class Foobar
                {
                    /**
                     * @ORM\Id
                     * @ORM\GeneratedValue(strategy="AUTO")
                     * @ORM\Column(type="integer")
                     */public ${'$'}id;
                }
            """.trimIndent(),
        )
    }
}
