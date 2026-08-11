package de.espend.idea.php.annotation.tests.doctrine.action

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrineAddRepositoryGenerateAction
 */
class DoctrineAddRepositoryGenerateActionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("DoctrineAddRepositoryGenerateAction.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/action/fixtures"
    }

    fun testGenerationForAnnotation() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App\Entity;
                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Entity()
                 */
                class User
                {<caret>
                }
            """.trimIndent(),
        )

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.DoctrineAddRepositoryGenerateAction")

        myFixture.checkResult(
            """
                <?php
                namespace App\Entity;
                use App\Entity\Repository\UserRepository;
                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Entity(repositoryClass=UserRepository::class)
                 */
                class User
                {
                }
            """.trimIndent(),
        )
    }

    fun testGenerationForAnnotationInsideDocCommentScope() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App\Entity;
                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Ent<caret>ity()
                 */
                class User
                {
                }
            """.trimIndent(),
        )

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.DoctrineAddRepositoryGenerateAction")

        myFixture.checkResult(
            """
                <?php
                namespace App\Entity;
                use App\Entity\Repository\UserRepository;
                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Entity(repositoryClass=UserRepository::class)
                 */
                class User
                {
                }
            """.trimIndent(),
        )
    }
}
