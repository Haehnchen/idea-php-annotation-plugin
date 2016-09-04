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
     * @see AnnotationPattern#getEnumPattern()
     */
    public void testGetEnumPattern() {
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
            assertTrue(AnnotationPattern.getEnumPattern().accepts(psiElement));
        }
    }

    /**
     * @see AnnotationPattern#getEnumPattern()
     */
    public void testGetEnumPatternForList() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo\n" +
            "{\n" +
            "    /**\n" +
            "     * @Route(methods  =  {   \"foobar\" , \"foo-bar\"  ,  \"<caret>\"})\n" +
            "     */\n" +
            "    private $foo;\n" +
            "}"
        );

        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertTrue(AnnotationPattern.getEnumPattern().accepts(psiElement));
    }
}
