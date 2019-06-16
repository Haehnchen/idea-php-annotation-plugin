package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationDeprecatedInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

public class AnnotationDeprecatedInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes_deprecated.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testThatInspectionIsDisplayedForAnnotationClasses() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                        "use Doctrine\\ORM\\Mapping as ORM;\n" +
                        "\n" +
                        "/**\n" +
                        " * @ORM\\E<caret>ntity()\n" +
                        " */\n" +
                        "class Foo\n" +
                        "{\n" +
                        "}",
                AnnotationDeprecatedInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                        "use Doctrine\\ORM\\Mapping as ORM;\n" +
                        "\n" +
                        "/**\n" +
                        " * @ORM\\Foo<caret>bar()\n" +
                        " */\n" +
                        "class Foo\n" +
                        "{\n" +
                        "}",
                AnnotationDeprecatedInspection.MESSAGE
        );
    }
}
