package de.espend.idea.php.annotation.tests.completion;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.completion.PhpAnnotationTypeCompletionProvider
 */
public class PhpAnnotationTypeCompletionProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/completion/fixtures";
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

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "/**\n" +
                "* @All(name=\"aa\",<caret>)\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "cascade", "option", "strategy"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "/**\n" +
                "* @All(name=\"aa\", <caret>)\n" +
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

    public void testNestedCompletion() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "function test() {" +
                "/** @Foo(foo=\"bar\", strategy={@All(<caret>)}) */" +
                "}\n" +
                "",
            "cascade"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "function test() {" +
                "/** @Foo(foo=\"bar\", strategy={@All(foo=\"foo\",<caret>)}) */" +
                "}\n" +
                "",
            "cascade"
        );
    }

    public void testNestedPropertyValueCompletion() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "function test() {" +
                "/** @Foo(foo=\"bar\", strategy={@All(foo=\"foo\",strategy=\"<caret>\")}) */" +
                "}\n" +
                "",
            "AUTO"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "function test() {" +
                "/** @Foo(foo=\"bar\", strategy={@All(foo=\"foo\", strategy=\"<caret>\")}) */" +
                "}\n" +
                "",
            "AUTO"
        );
    }

    public void testPropertyValueCompletionForMultipleTypes() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "/**\n" +
                "* @All(mixed=\"<caret>\")\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "true", "false"
        );
    }

    /**
     * in nested doc tag we have TEXT elements instead of WHITESPACE
     *
     * @see AnnotationPattern#getDocAttribute()
     */
    public void testNestedCompletionWithWhitespaceAsTextWorkaround() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;\n" +
                "function test() {" +
                "/** @Foo(foo=\"bar\", strategy={@All(foo=\"foo\", <caret>)}) */" +
                "}\n" +
                "",
            "cascade"
        );
    }
}
