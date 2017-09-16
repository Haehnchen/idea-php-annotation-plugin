package de.espend.idea.php.annotation.tests.inspection;

import de.espend.idea.php.annotation.inspection.AnnotationMissingUseInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

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
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
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
                "\n" +
                "\n" +
                "/**\n" +
                " * @Foo<caret>bar()\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            AnnotationMissingUseInspection.MESSAGE
        );
    }
}
