package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationMissingUseInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.inspection.AnnotationMissingUseInspection
 */
public class AnnotationMissingUseInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/inspection/fixtures";
    }

    public void testThatInspectionIsDisplayedForAnnotationClasses() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "\n" +
                "\n" +
                "/**\n" +
                " * @E<caret>ntity()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foo\\Entity;\n" +
                "\n" +
                "/**\n" +
                " * @E<caret>ntity()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "\n" +
                "/**\n" +
                " * @\\E<caret>ntity()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );
    }

    public void testThatInspectionIsNotDisplayedForClassesWhichDoesNotHaveAValidImportPath() {
        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foo\\Entity;\n" +
                "\n" +
                "/**\n" +
                " * @Entity()\n" +
                " * @Foo()\n" +
                " * @Fo<caret>obar()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );
    }

    public void testThatInspectionIsDisplayedForAnnotationClassesWithAlias() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\E<caret>ntity()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "use Foo\\Bar as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\E<caret>ntity()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );
    }

    public void testThatBlacklistedAnnotationDoesNotProvideInpsectionMessage() {
        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "\n" +
                "\n" +
                "/**\n" +
                " * @Annot<caret>ation()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );

        assertLocalInspectionContainsNotContains("test.php", "<?php\n" +
                "\n" +
                "\n" +
                "/**\n" +
                " * @noinspe<caret>ction()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );
    }
}
