package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationDocBlockConstantDeprecatedInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @see de.espend.idea.php.annotation.inspection.AnnotationDocBlockConstantDeprecatedInspection
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocBlockConstantDeprecatedInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testTheClassConstantProvidesNotificationForDeprecatedClassUsage() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBarDeprecated;\n" +
                "/**\n" +
                " * @Foobar(type=FooBarDeprecated::cl<caret>ass)\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBar;\n" +
                "/**\n" +
                " * @Foobar(type=FooBarDeprecated::cl<caret>ass)\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE
        );
    }

    public void testTheConstantProvidesNotificationForDeprecatedUsage() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBarDeprecated;\n" +
                "/**\n" +
                " * @Foobar(type=FooBarDeprecated::I_AM_DEP<caret>RECATED)\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBarDeprecated;\n" +
                "/**\n" +
                " * @Foobar(type=FooBarDeprecated::I_AM_NOT_DEPRECATED)\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockConstantDeprecatedInspection.MESSAGE
        );
    }
}
