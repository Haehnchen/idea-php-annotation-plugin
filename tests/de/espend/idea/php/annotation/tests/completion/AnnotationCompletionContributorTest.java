package de.espend.idea.php.annotation.tests.completion;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationCompletionContributorTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testDocTagCompletionInClassScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* <caret>" +
                "*/" +
                "class Foo {}",
            "All", "Clazz"
        );

        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* <caret>" +
                "*/" +
                "class Foo {}",
            "Property"
        );
    }

    public void testDocTagCompletionInClassPropertyScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "class Foo {\n" +
                "  /**" +
                "   * <caret>" +
                "   */" +
                "  var $foo;" +
                "}",
            "Property", "All"
        );

        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "class Foo {\n" +
                "  /**" +
                "   * <caret>" +
                "   */" +
                "  var $foo;" +
                "}",
            "Clazz"
        );
    }

    public void testDocTagCompletionInClassMethodScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "class Foo {\n" +
                "  /**" +
                "   * <caret>" +
                "   */" +
                "  function foo() {}" +
                "}",
            "Method", "All"
        );

        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "class Foo {\n" +
                "  /**" +
                "   * <caret>" +
                "   */" +
                "  function foo() {}" +
                "}",
            "Property", "Clazz"
        );
    }

    public void testCompletionOfClassConstants() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;" +
                "use \\My\\Annotations\\Constants\n" +
                "/**\n" +
                "* @All(Constants::<caret>)\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "FOO"
        );
    }
}
