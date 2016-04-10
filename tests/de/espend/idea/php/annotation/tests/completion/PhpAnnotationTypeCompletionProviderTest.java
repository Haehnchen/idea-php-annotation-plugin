package de.espend.idea.php.annotation.tests.completion;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpAnnotationTypeCompletionProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testDocTagPropertyCompletionInClassMethodScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "/**\n" +
                "* @All(<caret>)\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "cascade", "option", "strategy"
        );
    }

    public void testDocTagPropertyValueCompletionInClassMethodScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "/**\n" +
                "* @All(strategy=\"<caret>\")\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "AUTO"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "/**\n" +
                "* @All(option=\"<caret>\")\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "true", "false"
        );
    }

    public void testDocTagInlineCompletion() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "function test() {" +
                "/** @All(<caret>) */" +
                "}\n" +
                "",
            "cascade"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "function test() {" +
                "/** @All(strategy=\"<caret>\") */" +
                "}\n" +
                "",
            "AUTO"
        );
    }
}
