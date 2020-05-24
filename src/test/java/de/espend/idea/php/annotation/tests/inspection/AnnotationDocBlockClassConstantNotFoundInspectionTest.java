package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationDocBlockClassConstantNotFoundInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @see de.espend.idea.php.annotation.inspection.AnnotationDocBlockClassConstantNotFoundInspection
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocBlockClassConstantNotFoundInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testThatClassConstantProvideMissingUseHighlight() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "use Foo;\n" +
                "/**\n" +
                " * @Foobar(type=Foo\\Unknown::cl<caret>ass)\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "/**\n" +
                " * @Foobar(type=\\Foobar\\Bar\\FooBar::cl<caret>ass)\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBar;\n" +
                "/**\n" +
                " * @Foobar(type=FooBar::cl<caret>ass)\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar;\n" +
                "/**\n" +
                " * @Foobar(type=FooBar\\FooBar::cl<caret>ass)\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockClassConstantNotFoundInspection.MESSAGE
        );
    }
}
