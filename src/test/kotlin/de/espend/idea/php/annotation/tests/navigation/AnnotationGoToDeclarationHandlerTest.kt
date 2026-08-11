package de.espend.idea.php.annotation.tests.navigation

import com.intellij.patterns.PlatformPatterns
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.navigation.AnnotationGoToDeclarationHandler
 */
class AnnotationGoToDeclarationHandlerTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/java/de/espend/idea/php/annotation/tests/navigation/fixtures"

    fun testThatPhpDocOfNamespaceProvidesNavigation() {
        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use Foo\\Bar;\n" +
                    "\n" +
                    "/**\n" +
                    " * @B<caret>ar()\n" +
                    " */\n" +
                    "class Foo\n" +
                    "{}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java)
        )
    }

    fun testThatPhpDocOfFileScopeProvidesNavigation() {
        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Foo\\Bar;\n" +
                    "\n" +
                    "/**\n" +
                    " * @B<caret>ar()\n" +
                    " */\n" +
                    "class Foo\n" +
                    "{}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java)
        )
    }

    fun testThatPhpDocOfInlineProvidesNavigation() {
        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "use Foo\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @B<caret>ar */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java)
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use Foo\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @B<caret>ar */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java)
        )
    }

    fun testThatPropertyProvidesNavigation() {
        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "use Foo\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Bar(f<caret>oo=\"bar\") */" +
                    "}\n",
            PlatformPatterns.psiElement<Field?>(Field::class.java).withName("foo")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "use Foo\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(bla=\"\", foo={@Bar(f<caret>oo=\"bar\")}) */" +
                    "}\n",
            PlatformPatterns.psiElement<Field?>(Field::class.java).withName("foo")
        )
    }

    fun testThatClassContainsProvidesNavigation() {
        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use \\My\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(Bar::MY_<caret>VAR) */" +
                    "}\n",
            PlatformPatterns.psiElement<Field?>(Field::class.java).withName("MY_VAR")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(\\My\\Bar::cla<caret>ss) */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java).withName("Bar")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use \\My\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(Bar::cla<caret>ss) */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java).withName("Bar")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use \\My\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(B<caret>ar::MY_VAR) */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java).withName("Bar")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use \\My;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(My\\B<caret>ar::MY_VAR) */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java).withName("Bar")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use \\My\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(name={Bar::MY_<caret>VAR}) */" +
                    "}\n",
            PlatformPatterns.psiElement<Field?>(Field::class.java).withName("MY_VAR")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use \\My\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(name={B<caret>ar::MY_VAR}) */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java).withName("Bar")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace My;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(name={B<caret>ar::MY_VAR}) */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java).withName("Bar")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace My;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(name={Bar::clas<caret>s}) */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java).withName("Bar")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace My;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Foo(name={SubClass\\SubClassBar::cla<caret>ss}) */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpClass?>(PhpClass::class.java).withName("SubClassBar")
        )
    }

    fun testNavigationForPropertyInsideAnnotationAttributes() {
        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use Foo\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Bar(fo<caret>o=\"test\") */" +
                    "}\n",
            PlatformPatterns.psiElement<Field?>(Field::class.java).withName("foo")
        )

        assertNavigationMatch(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace Bar;\n" +
                    "\n" +
                    "use Foo\\Bar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /** @Bar(access<caret>Control=\"test\") */" +
                    "}\n",
            PlatformPatterns.psiElement<PhpDocTag?>(PhpDocTag::class.java)
        )
    }
}
