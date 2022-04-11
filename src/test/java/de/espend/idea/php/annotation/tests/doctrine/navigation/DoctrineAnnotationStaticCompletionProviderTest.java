package de.espend.idea.php.annotation.tests.doctrine.navigation;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineAnnotationStaticCompletionProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/navigation/fixtures";
    }

    public void testThatAttributeJoinColumnOnDeleteIsCompleted() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "    #[ORM\\JoinColumn(onDelete: '<caret>')]\n" +
                "    private $foo;\n" +
                "}",
            "CASCADE", "SET NULL"
        );

        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "    #[ORM\\JoinColumn(onDelete: ['<caret>'])]\n" +
                "    private $foo;\n" +
                "}",
            "CASCADE", "SET NULL"
        );
    }

    public void testThatAnnotationJoinColumnOnDeleteIsCompleted() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "    /** @ORM\\JoinColumn(onDelete=\"<caret>\") */\n\n" +
                "    private $foo;\n" +
                "}",
            "CASCADE", "SET NULL"
        );

        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "    /** @ORM\\JoinColumn(onDelete={\"<caret>\"}) */\n\n" +
                "    private $foo;\n" +
                "}",
            "CASCADE", "SET NULL"
        );
    }
}
