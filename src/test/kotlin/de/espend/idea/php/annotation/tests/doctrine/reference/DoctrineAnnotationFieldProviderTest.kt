package de.espend.idea.php.annotation.tests.doctrine.reference

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @see de.espend.idea.php.annotation.doctrine.reference.DoctrineAnnotationFieldProvider
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineAnnotationFieldProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/doctrine/reference/fixtures"

    fun testThatDoctrineRelationPropertiesProvideFieldCompletion() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    /** @ORM\\ManyToMany(targetEntity=\"My\\FooClass\\Bar\", mappedBy=\"<caret>\") */\n" +
                    "    protected \$logo;\n" +
                    "}",
            "bar"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    /** @ORM\\ManyToMany(targetEntity=\"My\\FooClass\\Bar\", inversedBy=\"<caret>\") */\n" +
                    "    protected \$logo;\n" +
                    "}",
            "bar"
        )

        // not working right now
        //assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
        //        "use Doctrine\\ORM\\Mapping as ORM;\n" +
        //        "class Foo\n" +
        //        "{\n" +
        //        "    /** @ORM\\ManyToMany(targetEntity=\\My\\FooClass\\Bar::class, mappedBy=\"<caret>\") */\n" +
        //        "    protected $logo;\n" +
        //        "}",
        //    "bar"
        //);
    }

    fun testThatDoctrineRelationAttributePropertiesProvideFieldCompletion() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "   #[ORM\\ManyToMany(targetEntity: \\My\\FooClass\\Bar2::class, mappedBy: \"<caret>\")]" +
                    "   protected \$logo;\n" +
                    "}",
            "bar2"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "   #[ORM\\ManyToMany(targetEntity: \\My\\FooClass\\Bar2::class, inversedBy: \"<caret>\")]" +
                    "   protected \$logo;\n" +
                    "}",
            "bar2"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "class Foo\n" +
                    "{\n" +
                    "   #[ORM\\ManyToMany(targetEntity: Foo::class, inversedBy: \"<caret>\")]" +
                    "   private \$foobar;\n" +
                    "}",
            "foobar"
        )
    }
}
