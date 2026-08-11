package de.espend.idea.php.annotation.tests.doctrine.reference

import com.intellij.patterns.PlatformPatterns
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

class DoctrineAnnotationTypeProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/reference/fixtures"

    fun testEntityRepositoryClassProvidesReference() {
        assertReferenceMatchOnParent(
            PhpFileType.INSTANCE,
            """
                <?php
                use Doctrine\ORM\Mapping as ORM;

                /** @ORM\Entity(repositoryClass="App\Repository\UserRepo<caret>sitory") */
                class User {}
            """.trimIndent(),
            PlatformPatterns.psiElement(PhpClass::class.java).withName("UserRepository"),
        )
    }

    fun testEntityRepositoryClassProvidesCompletion() {
        assertCompletionContains(
            PhpFileType.INSTANCE,
            """
                <?php
                use Doctrine\ORM\Mapping as ORM;

                /** @ORM\Entity(repositoryClass="<caret>") */
                class User {}
            """.trimIndent(),
            "App\\Repository\\UserRepository",
        )
    }
}
