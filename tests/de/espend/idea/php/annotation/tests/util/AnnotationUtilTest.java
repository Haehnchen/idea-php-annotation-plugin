package de.espend.idea.php.annotation.tests.util;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.util.AnnotationUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.php.annotation.util.AnnotationUtil
 */
public class AnnotationUtilTest extends LightCodeInsightFixtureTestCase {

    public void testIsAnnotationClass() {
        assertTrue(AnnotationUtil.isAnnotationClass(
            PhpPsiElementFactory.createPhpPsiFromText(getProject(), PhpClass.class, "<?php\n" +
            "/**\n" +
            "* @Annotation\n" +
            "*/\n" +
            "class Foo() {}\n"
        )));

        assertTrue(AnnotationUtil.isAnnotationClass(
            PhpPsiElementFactory.createPhpPsiFromText(getProject(), PhpClass.class, "<?php\n" +
            "/**\n" +
            "* @Annotation()\n" +
            "*/\n" +
            "class Foo() {}\n"
        )));

        assertFalse(AnnotationUtil.isAnnotationClass(
            PhpPsiElementFactory.createPhpPsiFromText(getProject(), PhpClass.class, "<?php\n" +
            "/**\n" +
            "* @Foo\n" +
            "*/\n" +
            "class Foo() {}\n"
        )));
    }

}
