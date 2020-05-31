package de.espend.idea.php.annotation.tests.util;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpDocUtilTest extends AnnotationLightCodeInsightFixtureTestCase {

    /**
     * @see de.espend.idea.php.annotation.util.PhpDocUtil#getNamespaceForDocIdentifier
     */
    public void testNamespaceIsExtractOnStopChar() {
        assertEquals("Foo\\Kernel", getLeftPsiElementString("@DateTime(Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("\\Foo\\Kernel", getLeftPsiElementString("@DateTime(\\Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("\\Foo\\Kernel", getLeftPsiElementString("@DateTime(\\Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Foo\\Kernel", getLeftPsiElementString("@DateTime(foo=Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Foo\\Kernel", getLeftPsiElementString("@DateTime(foo={Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Foo\\Kernel", getLeftPsiElementString("@DateTime({Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Foo\\Kernel", getLeftPsiElementString("@DateTime(\"Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Kernel", getLeftPsiElementString("@DateTime(Ker<caret>nel::VERSION)"));
        assertEquals("\\Foo\\Kernel", getLeftPsiElementString("@DateTime({\\Foo\\Ker<caret>nel::VERSION)"));
    }

    /**
     * @see de.espend.idea.php.annotation.util.PhpDocUtil#getNamespaceForDocIdentifierAtStart
     */
    public void testNamespaceIsExtractOnEndChar() {
        assertEquals("Foo\\Kernel", getRightPsiElementString("@DateTime(Fo<caret>o\\Kernel::VERSION)"));
        assertEquals("\\Foo\\Kernel", getRightPsiElementString("@DateTime(\\Fo<caret>o\\Kernel::VERSION)"));
        assertEquals("Foo\\Kernel", getRightPsiElementString("@DateTime(foo=Fo<caret>o\\Kernel::VERSION)"));
        assertEquals("Foo\\Kernel", getRightPsiElementString("@DateTime(foo={F<caret>oo\\Kernel::VERSION)"));
        assertEquals("\\Foo\\Kernel", getRightPsiElementString("@DateTime(foo={\\F<caret>oo\\Kernel::VERSION)"));
        assertEquals("Foo\\Kernel", getRightPsiElementString("@DateTime({F<caret>oo\\Kernel::VERSION)"));
        assertEquals("Foo\\Kernel", getRightPsiElementString("@DateTime(\"F<caret>oo\\Kernel::VERSION)"));
        assertEquals("Kernel", getRightPsiElementString("@DateTime(Ker<caret>nel::VERSION)"));

        assertNull(getRightPsiElementString("@DateTime(Ker<caret>nel)"));
        assertNull(getRightPsiElementString("@DateTime(Ker<caret>nel\\Test)"));
        assertNull(getRightPsiElementString("@DateTime(Ker<caret>nel Test::test)"));
        assertNull(getRightPsiElementString("@DateTime(Ker<caret>nel(Test::test))"));
    }

    /**
     * @see de.espend.idea.php.annotation.util.PhpDocUtil#isFirstIdentifierInNamespace
     */
    public void testThatIdentifierIsFirstElement() {
        assertTrue(getIsFirstIdentifierInNamespace("@DateTime(Fo<caret>o\\Kernel::VERSION)"));
        assertTrue(getIsFirstIdentifierInNamespace("@DateTime(\\Fo<caret>o\\Kernel::VERSION)"));
        assertTrue(getIsFirstIdentifierInNamespace("@DateTime(foo=Fo<caret>o\\Kernel::VERSION)"));
        assertTrue(getIsFirstIdentifierInNamespace("@DateTime(foo={\\F<caret>oo\\Kernel::VERSION)"));
        assertTrue(getIsFirstIdentifierInNamespace("@DateTime({F<caret>oo\\Kernel::VERSION)"));
        assertTrue(getIsFirstIdentifierInNamespace("@DateTime(\"F<caret>oo\\Kernel::VERSION)"));
        assertTrue(getIsFirstIdentifierInNamespace("@DateTime(Ker<caret>nel::VERSION)"));

        assertFalse(getIsFirstIdentifierInNamespace("@DateTime(\\Kernel\\Fo<caret>o::VERSION)"));
        assertFalse(getIsFirstIdentifierInNamespace("@DateTime(foo=Foo\\Kern<caret>el::VERSION)"));
        assertFalse(getIsFirstIdentifierInNamespace("@DateTime(foo={\\Foo\\Ker<caret>nel::VERSION)"));
    }

    private String getLeftPsiElementString(@NotNull String content) {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php" +
            "/**\n" +
            " * " + content + "\n" +
            "*/"
        );

        return PhpDocUtil.getNamespaceForDocIdentifier(
            myFixture.getFile().findElementAt(myFixture.getCaretOffset())
        );
    }

    private String getRightPsiElementString(@NotNull String content) {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php" +
            "/**\n" +
            " * " + content + "\n" +
            "*/"
        );

        return PhpDocUtil.getNamespaceForDocIdentifierAtStart(
            myFixture.getFile().findElementAt(myFixture.getCaretOffset())
        );
    }

    private boolean getIsFirstIdentifierInNamespace(@NotNull String content) {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php" +
            "/**\n" +
            " * " + content + "\n" +
            "*/"
        );

        return PhpDocUtil.isFirstIdentifierInNamespace(
            myFixture.getFile().findElementAt(myFixture.getCaretOffset())
        );
    }
}
