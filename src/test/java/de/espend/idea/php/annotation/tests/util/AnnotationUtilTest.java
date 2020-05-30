package de.espend.idea.php.annotation.tests.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.dict.PhpAnnotation;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.util.AnnotationUtil;

import java.util.*;

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
        return "src/test/java/de/espend/idea/php/annotation/tests/util/fixtures";
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
    public void testGetAnnotationsOnTargetMap1() {
        assertTrue(AnnotationUtil.getAnnotationsOnTargetMap(getProject(), AnnotationTarget.UNDEFINED).containsKey("My\\Annotations\\Undefined"));
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

    public void testGetUseImportMap() {
        PhpDocTag phpDocTag = PhpPsiElementFactory.createFromText(getProject(), PhpDocTag.class, "<?php\n" +
            "use Foobar;\n" +
            "use Bar as MyFoo" +
            "\n" +
            "/**\n" +
            " * @Foo()\n" +
            " **/\n" +
            "class Foo() {}\n"
        );

        Map<String, String> propertyForEnum = AnnotationUtil.getUseImportMap(phpDocTag);

        assertEquals("\\Foobar", propertyForEnum.get("Foobar"));
        assertEquals("\\Bar", propertyForEnum.get("MyFoo"));
    }

    public void testGetPropertyValueOrDefault() {
        Collection<String[]> dataProvider = new ArrayList<String[]>() {{
            add(new String[] {"/** @Template(\"Foobar\") */", "property", "Foobar"});
            add(new String[] {"/** @Template(name=\"Foo\") */", "name", "Foo"});
            add(new String[] {"/** @Template(\"Foobar\", name=\"Foo\") */", "name", "Foo"});
            add(new String[] {"/** @Template(\"Foobar\", foo=\"Foo\") */", "name", "Foobar"});
            add(new String[] {"/** @Template() */", "property", null});
        }};

        for (String[] strings : dataProvider) {
            PhpDocTag phpDocTag = PhpPsiElementFactory.createPhpPsiFromText(getProject(), PhpDocTag.class, "<?php\n" + strings[0]);
            assertEquals(strings[2], AnnotationUtil.getPropertyValueOrDefault(phpDocTag, strings[1]));
        }
    }

    public void testGetPropertyValue() {
        Collection<String[]> dataProvider = new ArrayList<String[]>() {{
            add(new String[] {"/** @Template(\"Foobar\") */", "property", null});
            add(new String[] {"/** @Template(name=\"Foo\") */", "name", "Foo"});
            add(new String[] {"/** @Template(\"Foobar\", name=\"Foo\") */", "name", "Foo"});
            add(new String[] {"/** @Template(\"Foobar\", foo=\"Foo\") */", "name", null});
            add(new String[] {"/** @Template(\"Foobar\", foo=FOO::class) */", "foo", null});
        }};

        for (String[] strings : dataProvider) {
            PhpDocTag phpDocTag = PhpPsiElementFactory.createPhpPsiFromText(getProject(), PhpDocTag.class, "<?php\n" + strings[0]);
            assertEquals(strings[2], AnnotationUtil.getPropertyValue(phpDocTag, strings[1]));
        }
    }

    public void testGetPropertyValueAsPsiElement() {
        PhpDocTag phpDocTag = PhpPsiElementFactory.createPhpPsiFromText(
            getProject(),
            PhpDocTag.class, "<?php\n /** @Template(\"Foobar\", name=\"Foo\") */"
        );

        assertEquals("Foo", AnnotationUtil.getPropertyValueAsPsiElement(phpDocTag, "name").getContents());
    }

    public void testGetPropertyValueAsElement() {
        PhpDocTag phpDocTag = PhpPsiElementFactory.createPhpPsiFromText(
            getProject(),
            PhpDocTag.class, "<?php\n /** @Template(\"Foobar\", name=\"Foo\") */"
        );

        assertEquals("Foo", ((StringLiteralExpression) AnnotationUtil.getPropertyValueAsElement(phpDocTag, "name")).getContents());
    }

    public void testGetClassAnnotation() {
        PhpClass phpClass = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "/**\n" +
            "* @Annotation\n" +
            "* @Target(\"PROPERTY\")\n" +
            "*/\n" +
            "class Foo {}\n"
        );

        PhpAnnotation classAnnotation = AnnotationUtil.getClassAnnotation(phpClass);
        List<AnnotationTarget> targets = classAnnotation.getTargets();

        assertContainsElements(targets, AnnotationTarget.PROPERTY);
    }

    public void testGetClassAnnotationAsArray() {
        PhpClass phpClass = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "/**\n" +
            "* @Annotation\n" +
            "* @Target(\"PROPERTY\", \"METHOD\")\n" +
            "* @Target(\"ALL\")\n" +
            "*/\n" +
            "class Foo {}\n"
        );

        PhpAnnotation classAnnotation = AnnotationUtil.getClassAnnotation(phpClass);
        List<AnnotationTarget> targets = classAnnotation.getTargets();

        assertContainsElements(targets, AnnotationTarget.PROPERTY, AnnotationTarget.METHOD, AnnotationTarget.ALL);
    }

    public void testGetClassAnnotationAsUnknown() {
        Collection<Object[]> dataProvider = new ArrayList<Object[]>() {{
            add(new Object[] {"@Target()", AnnotationTarget.UNKNOWN});
            add(new Object[] {"@Target", AnnotationTarget.UNKNOWN});
            add(new Object[] {"", AnnotationTarget.UNDEFINED});
        }};

        for (Object[] objects: dataProvider) {
            PhpClass phpClass = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
                "/**\n" +
                "* @Annotation\n" +
                "* "+ objects[0] +"\n" +
                "*/\n" +
                "class Foo {}\n"
            );

            PhpAnnotation classAnnotation = AnnotationUtil.getClassAnnotation(phpClass);
            List<AnnotationTarget> targets = classAnnotation.getTargets();

            assertContainsElements(targets, objects[1]);
        }
    }

    public void testThatImportForClassIsSuggestedForImportedClass() {
        myFixture.copyFileToProject("doctrine.php");

        PhpDocTag phpDocTag = PhpPsiElementFactory.createFromText(getProject(), PhpDocTag.class, "<?php\n" +
            "/**\n" +
            "* @Entity()\n" +
            "*/\n" +
            "class Foo {}\n"
        );

        Map<String, String> possibleImportClasses = AnnotationUtil.getPossibleImportClasses(phpDocTag);
        assertNull(possibleImportClasses.get("\\Doctrine\\ORM\\Mapping"));
    }

    public void testThatImportForClassIsSuggestedForAliasImportClass() {
        myFixture.copyFileToProject("doctrine.php");

        PhpDocTag phpDocTag = PhpPsiElementFactory.createFromText(getProject(), PhpDocTag.class, "<?php\n" +
            "/**\n" +
            "* @ORM\\Entity()\n" +
            "*/\n" +
            "class Foo {}\n"
        );

        Map<String, String> possibleImportClasses = AnnotationUtil.getPossibleImportClasses(phpDocTag);
        assertEquals("ORM", possibleImportClasses.get("\\Doctrine\\ORM\\Mapping"));
    }

    public void testAttributeVisitingForAnnotationClass() {
        myFixture.copyFileToProject("doctrine.php");

        PhpClass phpClass = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "/**\n" +
            "* @Attributes(\n" +
            "*    @Attribute(\"accessControl\", type=\"string\"),\n" +
            "*    @Attribute(\"accessControl2\", type=\"string\"),\n" +
            "* )\n" +
            "*\n" +
            "* @Attributes({\n" +
            "*    @Attribute(\"array\", type=\"array\"),\n" +
            "*    @Attribute(\"array2\", type=\"array\"),\n" +
            "* })\n" +
            "*/\n" +
            "class Foo\n" +
            "{" +
            "   public string $foo;\n" +
            "   \n" +
            "   /** @var boolean **/\n" +
            "   public $bool;\n" +
            "   public $myArray = [];\n" +
            "   public $myBool = false;\n" +
            "   public ?string $nullable;\n" +
            "}\n"
        );

        Map<String, String> attributes = new HashMap<>();

        AnnotationUtil.visitAttributes(phpClass, (attribute, type, psiElement) -> {
            attributes.put(attribute, type);
            return null;
        });

        assertContainsElements(attributes.keySet(), "accessControl", "accessControl2", "array", "array2", "foo");

        assertEquals("array", attributes.get("array2"));
        assertEquals("string", attributes.get("accessControl"));
        assertEquals("bool", attributes.get("bool"));

        assertEquals("array", attributes.get("myArray"));
        assertEquals("bool", attributes.get("myBool"));
        assertEquals("string", attributes.get("nullable"));
    }
}
