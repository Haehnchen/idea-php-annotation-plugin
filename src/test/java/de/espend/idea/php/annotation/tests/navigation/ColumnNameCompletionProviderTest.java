package de.espend.idea.php.annotation.tests.navigation;

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ColumnNameCompletionProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("ColumnNameCompletionProvider.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/navigation/fixtures";
    }

    public void testThatDoctrineORMColumnTypeStringIsCompletedForAnnotation() {
        assertCompletionContains("test.php", "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\Column(type=\"json<caret>\")\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            "json_array"
        );
    }

    public void testThatDoctrineORMColumnTypeStringIsCompletedForAttribute() {
        assertCompletionContains("test.php", "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "#[ORM\\Column(type: 'json<caret>')]\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            "json_array"
        );
    }
}
