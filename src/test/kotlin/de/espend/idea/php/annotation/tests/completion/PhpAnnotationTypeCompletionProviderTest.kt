package de.espend.idea.php.annotation.tests.completion

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.pattern.AnnotationPattern
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.completion.PhpAnnotationTypeCompletionProvider
 */
class PhpAnnotationTypeCompletionProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/completion/fixtures"

    fun testDocTagPropertyCompletionInClassMethodScope() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "/**\n" +
                    "* @All(<caret>)\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "cascade", "option", "strategy"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "/**\n" +
                    "* @All(name=\"aa\",<caret>)\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "cascade", "option", "strategy"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "/**\n" +
                    "* @All(name=\"aa\", <caret>)\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "cascade", "option", "strategy"
        )
    }

    fun testDocTagPropertyValueCompletionInClassMethodScope() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "/**\n" +
                    "* @All(strategy=\"<caret>\")\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "AUTO"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "/**\n" +
                    "* @All(option=\"<caret>\")\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "true", "false"
        )
    }

    fun testAttributePropertyValueCompletionInClassMethodScope() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "#[All(strategy: '<caret>')]\n" +
                    "class Foo {}\n",
            "AUTO"
        )

        assertCompletionNotContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "#[All(strategy: ['<caret>'])]\n" +
                    "class Foo {}\n",
            "AUTO"
        )
    }

    fun testDocTagInlineCompletion() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "function test() {" +
                    "/** @All(<caret>) */" +
                    "}\n" +
                    "",
            "cascade"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "function test() {" +
                    "/** @All(strategy=\"<caret>\") */" +
                    "}\n" +
                    "",
            "AUTO"
        )
    }

    fun testNestedCompletion() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "function test() {" +
                    "/** @Foo(foo=\"bar\", strategy={@All(<caret>)}) */" +
                    "}\n" +
                    "",
            "cascade"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "function test() {" +
                    "/** @Foo(foo=\"bar\", strategy={@All(foo=\"foo\",<caret>)}) */" +
                    "}\n" +
                    "",
            "cascade"
        )
    }

    fun testNestedPropertyValueCompletion() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "function test() {" +
                    "/** @Foo(foo=\"bar\", strategy={@All(foo=\"foo\",strategy=\"<caret>\")}) */" +
                    "}\n" +
                    "",
            "AUTO"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "function test() {" +
                    "/** @Foo(foo=\"bar\", strategy={@All(foo=\"foo\", strategy=\"<caret>\")}) */" +
                    "}\n" +
                    "",
            "AUTO"
        )
    }

    fun testPropertyValueCompletionForTypes() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "/**\n" +
                    "* @All(boolValue=\"<caret>\")\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "true", "false"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "/**\n" +
                    "* @All(has_access=\"<caret>\")\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "true", "false"
        )
    }

    fun testAttributePropertyValueCompletionForTypes() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "#[All(boolValue: '<caret>')]\n" +
                    "class Foo {}\n",
            "true", "false"
        )

        assertCompletionNotContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "#[All(boolValue: ['<caret>'])]\n" +
                    "class Foo {}\n",
            "true"
        )
    }

    /**
     * in nested doc tag we have TEXT elements instead of WHITESPACE
     *
     * @see AnnotationPattern.getDocAttribute
     */
    fun testNestedCompletionWithWhitespaceAsTextWorkaround() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;\n" +
                    "function test() {" +
                    "/** @Foo(foo=\"bar\", strategy={@All(foo=\"foo\", <caret>)}) */" +
                    "}\n" +
                    "",
            "cascade"
        )
    }
}
