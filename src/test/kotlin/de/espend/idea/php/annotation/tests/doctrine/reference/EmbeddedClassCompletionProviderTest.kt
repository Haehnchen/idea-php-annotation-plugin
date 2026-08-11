package de.espend.idea.php.annotation.tests.doctrine.reference

import com.intellij.patterns.PlatformPatterns
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class EmbeddedClassCompletionProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/reference/fixtures"

    fun testThatDoctrineEmbeddedClassPropertyProvidesClassReferences() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    /** @ORM\\Embedded(class=\"<caret>\") */\n" +
                    "    protected \$logo;\n" +
                    "}",
            "Bar"
        )

        assertReferenceMatchOnParent(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    /** @ORM\\Embedded(class=\"My\\FooC<caret>lass\\Bar\") */\n" +
                    "    protected \$logo;\n" +
                    "}",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java)
        )
    }
}
