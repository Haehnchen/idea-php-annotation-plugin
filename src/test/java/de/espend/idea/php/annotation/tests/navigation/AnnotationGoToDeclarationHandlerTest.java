package de.espend.idea.php.annotation.tests.navigation;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.navigation.AnnotationGoToDeclarationHandler
 */
public class AnnotationGoToDeclarationHandlerTest extends AnnotationLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/navigation/fixtures";
    }

    public void testThatPhpDocOfNamespaceProvidesNavigation() {
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use Foo\\Bar;\n" +
                "\n" +
                "/**\n" +
                " * @B<caret>ar()\n" +
                " */\n" +
                "class Foo\n" +
                "{}\n",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }

    public void testThatPhpDocOfFileScopeProvidesNavigation() {
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "use Foo\\Bar;\n" +
                "\n" +
                "/**\n" +
                " * @B<caret>ar()\n" +
                " */\n" +
                "class Foo\n" +
                "{}\n",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }

    public void testThatPhpDocOfInlineProvidesNavigation() {
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "use Foo\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @B<caret>ar */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class)
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "use Foo\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @B<caret>ar */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }

    public void testThatPropertyProvidesNavigation() {
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "use Foo\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Bar(f<caret>oo=\"bar\") */" +
                "}\n",
            PlatformPatterns.psiElement(Field.class).withName("foo")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "use Foo\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(bla=\"\", foo={@Bar(f<caret>oo=\"bar\")}) */" +
                "}\n",
            PlatformPatterns.psiElement(Field.class).withName("foo")
        );
    }

    public void testThatClassContainsProvidesNavigation() {
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use \\My\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(Bar::MY_<caret>VAR) */" +
                "}\n",
            PlatformPatterns.psiElement(Field.class).withName("MY_VAR")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(\\My\\Bar::cla<caret>ss) */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use \\My\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(Bar::cla<caret>ss) */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use \\My\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(B<caret>ar::MY_VAR) */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use \\My;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(My\\B<caret>ar::MY_VAR) */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use \\My\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(name={Bar::MY_<caret>VAR}) */" +
                "}\n",
            PlatformPatterns.psiElement(Field.class).withName("MY_VAR")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use \\My\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(name={B<caret>ar::MY_VAR}) */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace My;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(name={B<caret>ar::MY_VAR}) */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace My;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(name={Bar::clas<caret>s}) */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("Bar")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace My;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Foo(name={SubClass\\SubClassBar::cla<caret>ss}) */" +
                "}\n",
            PlatformPatterns.psiElement(PhpClass.class).withName("SubClassBar")
        );
    }

    public void testNavigationForPropertyInsideAnnotationAttributes() {
        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use Foo\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Bar(fo<caret>o=\"test\") */" +
                "}\n",
            PlatformPatterns.psiElement(Field.class).withName("foo")
        );

        assertNavigationMatch(PhpFileType.INSTANCE, "<?php\n" +
                "namespace Bar;\n" +
                "\n" +
                "use Foo\\Bar;\n" +
                "\n" +
                "class Foo\n" +
                "{\n" +
                "  /** @Bar(access<caret>Control=\"test\") */" +
                "}\n",
            PlatformPatterns.psiElement(PhpDocTag.class)
        );
    }
}
