package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationDocBlockTagClassNotFoundInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @see AnnotationDocBlockTagClassNotFoundInspection
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocBlockTagClassNotFoundInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testAbsoluteFqnProvidesCheckForClassExists() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "/**\n" +
                " * @\\Foobar\\Bar\\FooBa<caret>rNot()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "/**\n" +
                " * @\\Foobar\\Bar\\FooBa<caret>r()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );
    }

    public void testRelativeFqnProvidesCheckForClassExists() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "use Foobar\\CarNot;\n" +
                "/**\n" +
                " * @Car<caret>Not()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContains("test.php", "<?php\n" +
                "use Foobar\\Foobar;\n" +
                "/**\n" +
                " * @Foo<caret>bar()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContains("test.php", "<?php\n" +
                "use Foobar as Foo;\n" +
                "/**\n" +
                " * @Foo\\Ent<caret>ity()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        // no matches

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "/**\n" +
                " * @Bar\\Ent<caret>ityNot()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBar as Foo;\n" +
                "/**\n" +
                " * @Fo<caret>o()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar as Foo;\n" +
                "/**\n" +
                " * @Foo<caret>Bar()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBar;\n" +
                "/**\n" +
                " * @Foo<caret>Bar()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBar;\n" +
                "/**\n" +
                " * @phpst<caret>an-template\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBar;\n" +
                "/**\n" +
                " * @ps<caret>alm-foobar\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBar;\n" +
                "/**\n" +
                " * @phpcsSup<caret>press\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foobar\\Bar\\FooBar;\n" +
                "/**\n" +
                " * @foo<caret>bar\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationDocBlockTagClassNotFoundInspection.MESSAGE
        );
    }
}
