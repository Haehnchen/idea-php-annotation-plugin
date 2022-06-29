package de.espend.idea.php.annotation.tests.doctrine.reference;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @see de.espend.idea.php.annotation.doctrine.reference.DoctrineAnnotationFieldProvider
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineAnnotationFieldProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/reference/fixtures";
    }

    public void testThatDoctrineCustomIdGeneratorPropertyProvidesClassReferences() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "    /** @ORM\\ManyToMany(targetEntity=\"My\\FooClass\\Bar\", mappedBy=\"<caret>\") */\n" +
                "    protected $logo;\n" +
                "}",
            "bar"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "    /** @ORM\\ManyToMany(targetEntity=\"My\\FooClass\\Bar\", inversedBy=\"<caret>\") */\n" +
                "    protected $logo;\n" +
                "}",
            "bar"
        );

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

    public void testThatDoctrineCustomIdGeneratorPropertyProvidesClassReferences222() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "   #[ORM\\ManyToMany(targetEntity: \\My\\FooClass\\Bar2::class, mappedBy: \"<caret>\")]" +
                "   protected $logo;\n" +
                "}",
            "bar2"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "   #[ORM\\ManyToMany(targetEntity: \\My\\FooClass\\Bar2::class, inversedBy: \"<caret>\")]" +
                "   protected $logo;\n" +
                "}",
            "bar2"
        );
    }
}
