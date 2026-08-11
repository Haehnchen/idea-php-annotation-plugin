package de.espend.idea.php.annotation.tests.doctrine.navigation

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineAnnotationStaticCompletionProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/navigation/fixtures"

    fun testThatAttributeJoinColumnOnDeleteIsCompleted() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    #[ORM\\JoinColumn(onDelete: '<caret>')]\n" +
                    "    private \$foo;\n" +
                    "}",
            "CASCADE", "SET NULL"
        )

        assertCompletionNotContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    #[ORM\\JoinColumn(onDelete: ['<caret>'])]\n" +
                    "    private \$foo;\n" +
                    "}",
            "CASCADE", "SET NULL"
        )
    }

    fun testThatAnnotationJoinColumnOnDeleteIsCompleted() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    /** @ORM\\JoinColumn(onDelete=\"<caret>\") */\n\n" +
                    "    private \$foo;\n" +
                    "}",
            "CASCADE", "SET NULL"
        )

        assertCompletionNotContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    /** @ORM\\JoinColumn(onDelete={\"<caret>\"}) */\n\n" +
                    "    private \$foo;\n" +
                    "}",
            "CASCADE", "SET NULL"
        )
    }
}
