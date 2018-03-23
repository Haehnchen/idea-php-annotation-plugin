package de.espend.idea.php.annotation.tests.doctrine.inspection;

import de.espend.idea.php.annotation.doctrine.inspection.RepositoryClassInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.inspection.RepositoryClassInspection
 */
public class RepositoryClassInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/inspection/fixtures";
    }

    public void testThatInspectionForMissingClassIsProvided() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\Entity(repositoryClass=\"Foo<caret>bar\")\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            RepositoryClassInspection.MESSAGE
        );

        assertLocalInspectionContains("test.php", "<?php\n" +
                "\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\Entity(\"Foobar\", repositoryClass=\"Foo<caret>bar\")\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            RepositoryClassInspection.MESSAGE
        );
    }

    public void testThatExistingClassIsNotHighlighted() {
        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\Entity(repositoryClass=\"Foob<caret>ar\\Foo\")\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            RepositoryClassInspection.MESSAGE
        );
    }

    public void testThatExistingClassInSameNamespaceIsNotHighlighted() {
        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "\n" +
                "namespace Foobar;" +
                "" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\Entity(repositoryClass=\"F<caret>oo\")\n" +
                " */\n" +
                "class Bar\n" +
                "{\n" +
                "}",
            RepositoryClassInspection.MESSAGE
        );
    }
}
