package de.espend.idea.php.annotation.tests.doctrine.intention

import com.intellij.openapi.command.WriteCommandAction
import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention
 */
class DoctrineOrmRepositoryIntentionTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/intention/fixtures"
    }

    fun testThatRepositoryAnnotatorIsAvailableForAnnotation() {
        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Ent<caret>ity()
                 */
                class Relation
                {
                }
            """.trimIndent(),
            "Add Doctrine repository",
        )
    }

    fun testThatRepositoryAnnotatorIsAvailableForAttribute() {
        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                #[ORM\Ent<caret>ity()]class Relation
                {
                }
            """.trimIndent(),
            "Add Doctrine repository",
        )

        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                #[ORM\Entity("fo<caret>o")]class Relation
                {
                }
            """.trimIndent(),
            "Add Doctrine repository",
        )
    }

    fun testThatRepositoryAnnotatorForAttributeValueIsAvailable() {
        assertIntentionIsAvailable(
            PhpFileType.INSTANCE,
            """
                <?php

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\Entity(<caret>)
                 */
                class Relation
                {
                }
            """.trimIndent(),
            "Add Doctrine repository",
        )
    }

    fun testAttribute() {
        myFixture.configureByText(
            "test.php",
            """
                <?php

                namespace App\Entity;

                use App\Entity\Repository\UserRepository;
                use Doctrine\ORM\Mapping as ORM;

                #[ORM\Ent<caret>ity()]
                class User {}
            """.trimIndent(),
        )

        val text = invokeAndGetText()
        assertTrue(text.contains("use App\\Entity\\Repository\\UserRepository;"))
        assertTrue(text.contains("#[ORM\\Entity(repositoryClass: UserRepository::class)]"))
    }

    fun testAnnotation() {
        myFixture.configureByText(
            "test.php",
            """
                <?php

                namespace App\Entity;

                use Doctrine\ORM\Mapping as ORM;

                /**
                 * @ORM\En<caret>tity()
                 */
                class User {}
            """.trimIndent(),
        )

        val text = invokeAndGetText()
        assertTrue(text.contains("use App\\Entity\\Repository\\UserRepository;"))
        assertTrue(text.contains("@ORM\\Entity(repositoryClass=UserRepository::class)"))
    }

    private fun invokeAndGetText(): String {
        val psiElement = myFixture.file.findElementAt(myFixture.caretOffset)

        WriteCommandAction.runWriteCommandAction(project) {
            DoctrineOrmRepositoryIntention().invoke(project, editor, psiElement!!)
        }

        return editor.document.text
    }
}
