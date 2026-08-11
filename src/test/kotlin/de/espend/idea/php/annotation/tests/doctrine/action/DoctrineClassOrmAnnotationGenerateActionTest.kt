package de.espend.idea.php.annotation.tests.doctrine.action

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrineClassOrmAnnotationGenerateAction
 */
class DoctrineClassOrmAnnotationGenerateActionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/action/fixtures"
    }

    fun testThatThatEntityClassIsGeneratedForAnnotations() {
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

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator")

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @\Doctrine\ORM\Mapping\Entity
                 */
                class Existing {}

                /**
                 * @ORM\Entity
                 * @ORM\Table(name="foobar")
                 */
                class Foobar
                {
                   public ${'$'}id;
                }
            """.trimIndent(),
        )
    }

    fun testThatThatEntityClassIsGeneratedForAttribute() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                class Foobar
                {
                   #[Dummy]
                   public ${'$'}id;<caret>
                }
            """.trimIndent(),
        )

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator")

        myFixture.checkResult(
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                #[ORM\Entity]
                #[ORM\Table(name: 'foobar')]
                class Foobar
                {
                   #[Dummy]
                   public ${'$'}id;
                }
            """.trimIndent(),
        )
    }

    fun testThatThatEntityClassIsGeneratedForAttributeWithRepository() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App\Entity;

                class Car
                {
                   #[Dummy]
                   public ${'$'}id;<caret>
                }
            """.trimIndent(),
        )

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator")

        myFixture.checkResult(
            """
                <?php
                namespace App\Entity;

                use App\Entity\CarRepository;
                use Doctrine\ORM\Mapping as ORM;

                #[ORM\Entity(repositoryClass: App\Entity\CarRepository::class)]
                #[ORM\Table(name: 'car')]
                class Car
                {
                   #[Dummy]
                   public ${'$'}id;
                }
            """.trimIndent(),
        )
    }
}
