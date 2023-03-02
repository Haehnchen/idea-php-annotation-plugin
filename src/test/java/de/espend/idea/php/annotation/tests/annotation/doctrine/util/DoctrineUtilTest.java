package de.espend.idea.php.annotation.tests.annotation.doctrine.util;

import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineUtilTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/annotation/doctrine/util/fixtures";
    }

    public void testIsOrmColumnProperty() {
        PhpClass phpClass = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "class Foobar\n" +
            "{\n" +
            "    public $id;\n" +
            "    /**\n" +
            "    * @ORM\\Column()\n" +
            "    */\n" +
            "    public $createdAt;\n" +
            "    \n" +
            "    #[ORM\\Column(type: 'integer')]\n" +
            "    private int $attributeId;" +
            "}"
        );

        assertTrue(DoctrineUtil.isOrmColumnProperty(phpClass.findFieldByName("createdAt", false)));
        assertTrue(DoctrineUtil.isOrmColumnProperty(phpClass.findFieldByName("attributeId", false)));
        assertFalse(DoctrineUtil.isOrmColumnProperty(phpClass.findFieldByName("id", false)));
    }

    public void testHasRepositoryClassForAttribute() {
        PhpClass phpClass = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "#[ORM\\Entity()]" +
            "class Foobar\n" +
            "{\n" +
            "}"
        );

        assertTrue(DoctrineUtil.hasCreateRepositoryClassSupport(phpClass));

        PhpClass phpClass1 = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "#[ORM\\Entity(repositoryClass: Foobar::class)]" +
            "class Foobar\n" +
            "{\n" +
            "}"
        );

        assertFalse(DoctrineUtil.hasCreateRepositoryClassSupport(phpClass1));

        PhpClass phpClass2 = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "#[ORM\\Foo()]" +
            "class Foobar\n" +
            "{\n" +
            "}"
        );

        assertFalse(DoctrineUtil.hasCreateRepositoryClassSupport(phpClass2));
    }

    public void testHasRepositoryClassForAnnotation() {
        PhpClass phpClass = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Entity()\n" +
            " */\n" +
            "class Foobar\n" +
            "{\n" +
            "}"
        );

        assertTrue(DoctrineUtil.hasCreateRepositoryClassSupport(phpClass));

        PhpClass phpClass1 = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Entity(repositoryClass=\"Foo<caret>bar\")\n" +
            " */\n" +
            "class Foobar\n" +
            "{\n" +
            "}"
        );

        assertFalse(DoctrineUtil.hasCreateRepositoryClassSupport(phpClass1));

        PhpClass phpClass2 = PhpPsiElementFactory.createFromText(getProject(), PhpClass.class, "<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Foobar()\n" +
            " */\n" +
            "class Foobar\n" +
            "{\n" +
            "}"
        );

        assertFalse(DoctrineUtil.hasCreateRepositoryClassSupport(phpClass2));
    }
}
