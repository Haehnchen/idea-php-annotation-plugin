package de.espend.idea.php.annotation.tests.doctrine.reference

import com.intellij.patterns.PlatformPatterns
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

class DoctrineAnnotationFieldTypeProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/reference/fixtures"

    fun testColumnTypeProvidesCompletionAndReference() {
        val annotation = """
            <?php
            use Doctrine\ORM\Mapping as ORM;

            class User
            {
                /** @ORM\Column(type="my_<caret>type") */
                private ${'$'}name;
            }
        """.trimIndent()

        assertCompletionContains(PhpFileType.INSTANCE, annotation, "my_type")
        assertReferenceMatchOnParent(
            PhpFileType.INSTANCE,
            annotation,
            PlatformPatterns.psiElement(PhpClass::class.java).withName("MyType"),
        )
    }
}
