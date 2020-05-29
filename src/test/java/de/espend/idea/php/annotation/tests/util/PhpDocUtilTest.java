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
        assertEquals("Foo\\Kernel", getPsiElement("@DateTime(Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("\\Foo\\Kernel", getPsiElement("@DateTime(\\Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("\\Foo\\Kernel", getPsiElement("@DateTime(\\Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Foo\\Kernel", getPsiElement("@DateTime(foo=Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Foo\\Kernel", getPsiElement("@DateTime(foo={Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Foo\\Kernel", getPsiElement("@DateTime({Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Foo\\Kernel", getPsiElement("@DateTime(\"Foo\\Ker<caret>nel::VERSION)"));
        assertEquals("Kernel", getPsiElement("@DateTime(Ker<caret>nel::VERSION)"));
        assertEquals("\\Foo\\Kernel", getPsiElement("@DateTime({\\Foo\\Ker<caret>nel::VERSION)"));
    }

    private String getPsiElement(@NotNull String content) {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php" +
            "/**\n" +
            " * " + content + "\n" +
            "*/"
        );

        return PhpDocUtil.getNamespaceForDocIdentifier(
            myFixture.getFile().findElementAt(myFixture.getCaretOffset())
        );
    }
}
