package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineTypeDeprecatedInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("DoctrineTypeDeprecatedInspection.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testThatDeprecatedInspectionIsDisplayedDoctrineColumnTypes() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\Column(type=\"json<caret>_array\")\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            "[Annotations] Deprecated: Use JsonType instead"
        );

        assertLocalInspectionIsEmpty("test.php", "<?php\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Column(type=\"json<caret>\")\n" +
            " */\n" +
            "class Foo\n" +
            "{\n" +
            "}"
        );
    }
}
