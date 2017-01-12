package de.espend.idea.php.annotation.tests.pattern;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.pattern.AnnotationPattern
 */
public class AnnotationPatternTest extends AnnotationLightCodeInsightFixtureTestCase {

    /**
     * @see AnnotationPattern#getPropertyArrayPattern()
     */
    public void testGetPropertyArrayPattern() {
        for (String s : new String[]{"methods={\"<caret>\"}", "methods={ \"<caret>\" }", "methods    =    {       \"<caret>\" }"}) {
            myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo\n" +
                "{\n" +
                "    /**\n" +
                "     * @Route(" + s + ")\n" +
                "     */\n" +
                "    private $foo;\n" +
                "}"
            );

            PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
            assertTrue(AnnotationPattern.getPropertyArrayPattern().accepts(psiElement));
        }
    }

    /**
     * @see AnnotationPattern#getPropertyArrayPattern()
     */
    public void testGetPropertyArrayPatternForList() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo\n" +
            "{\n" +
            "    /**\n" +
            "     * @Route(methods  =  {   \"foobar\" , \"foo-bar\"  ,  \"<caret>\"})\n" +
            "     */\n" +
            "    private $foo;\n" +
            "}"
        );

        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertTrue(AnnotationPattern.getPropertyArrayPattern().accepts(psiElement));
    }

    /**
     * @see AnnotationPattern#getPropertyNameOfArrayValuePattern()
     */
    public void testGetEnumPropertyPattern() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo\n" +
            "{\n" +
            "    /**\n" +
            "     * @Route(met<caret>hods  =  {   \"foobar\" , \"foo-bar\"  ,  \"\"})\n" +
            "     */\n" +
            "    private $foo;\n" +
            "}"
        );

        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertTrue(AnnotationPattern.getPropertyNameOfArrayValuePattern().accepts(psiElement));
    }

    /**
     * @see AnnotationPattern#getDefaultPropertyValue()
     */
    public void testGetDefaultPropertyValue() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo\n" +
            "{\n" +
            "    /**\n" +
            "     * @Route(\"<caret>\")\n" +
            "     */\n" +
            "    private $foo;\n" +
            "}"
        );

        assertTrue(AnnotationPattern.getDefaultPropertyValue().accepts(myFixture.getFile().findElementAt(myFixture.getCaretOffset())));

        myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo\n" +
            "{\n" +
            "    /**\n" +
            "     * @Route(foobar=\"aaa\", \"<caret>\")\n" +
            "     */\n" +
            "    private $foo;\n" +
            "}"
        );

        assertTrue(AnnotationPattern.getDefaultPropertyValue().accepts(myFixture.getFile().findElementAt(myFixture.getCaretOffset())));

        myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo\n" +
            "{\n" +
            "    /**\n" +
            "     * @Route(\"<caret>\", foobar=\"aaa\")\n" +
            "     */\n" +
            "    private $foo;\n" +
            "}"
        );

        assertTrue(AnnotationPattern.getDefaultPropertyValue().accepts(myFixture.getFile().findElementAt(myFixture.getCaretOffset())));
    }
}
