package de.espend.idea.php.annotation.tests.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.util.AnnotationUtil;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * @see de.espend.idea.php.annotation.util.AnnotationUtil
 */
public class AnnotationUtilTest extends AnnotationLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("targets.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

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

    public void testGetAnnotationsOnTargetMap() {
        assertTrue(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.PROPERTY).containsKey("My\\Annotations\\Property"));
        assertTrue(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.ALL).containsKey("My\\Annotations\\All"));
        assertTrue(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.PROPERTY).containsKey("My\\Annotations\\PropertyMethod"));
        assertTrue(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.METHOD).containsKey("My\\Annotations\\PropertyMethod"));
        assertTrue(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.PROPERTY).containsKey("My\\Annotations\\PropertyMethodArray"));
        assertTrue(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.METHOD).containsKey("My\\Annotations\\PropertyMethodArray"));
        assertTrue(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.UNDEFINED).containsKey("My\\Annotations\\Undefined"));
        assertFalse(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.ALL).containsKey("My\\Annotations\\Unknown"));
    }

    public void testGetPropertyAndClassForArray() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "/**\n" +
            "* @Foo(name={\"FOOBAR\", \"FO<caret>OBAR2\"})n" +
            "*/\n" +
            "class Foo() {}\n"
        );

        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        PsiElement propertyForEnum = AnnotationUtil.getPropertyForArray((StringLiteralExpression) psiElement.getParent());

        assertNotNull(propertyForEnum);
        assertEquals("name", propertyForEnum.getText());
    }
}
