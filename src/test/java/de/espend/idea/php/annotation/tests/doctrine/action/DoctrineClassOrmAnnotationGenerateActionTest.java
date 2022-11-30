package de.espend.idea.php.annotation.tests.doctrine.action;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.doctrine.action.DoctrineClassGeneratorAction;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see DoctrineClassGeneratorAction
 */
public class DoctrineClassOrmAnnotationGenerateActionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/action/fixtures";
    }

    public void testThatThatEntityClassIsGeneratedForAnnotations() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "\n" +
            "class Foobar\n" +
            "{\n" +
            "   public $id;<caret>\n" +
            "}"
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator");

        myFixture.checkResult("<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Entity\n" +
            " * @ORM\\Table(name=\"foobar\")\n" +
            " */\n" +
            "class Foobar\n" +
            "{\n" +
            "   public $id;\n" +
            "}"
        );
    }

    public void testThatThatEntityClassIsGeneratedForAttribute() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "\n" +
            "class Foobar\n" +
            "{\n" +
            "   #[Dummy]\n" +
            "   public $id;<caret>\n" +
            "}"
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator");

        myFixture.checkResult("<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "#[ORM\\Entity]\n" +
            "#[ORM\\Table(name: 'foobar')]\n" +
            "class Foobar\n" +
            "{\n" +
            "   #[Dummy]\n" +
            "   public $id;\n" +
            "}"
        );
    }

    public void testThatThatEntityClassIsGeneratedForAttributeWithRepository() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "namespace App\\Entity;\n" +
            "\n" +
            "class Car\n" +
            "{\n" +
            "   #[Dummy]\n" +
            "   public $id;<caret>\n" +
            "}"
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.ClassGenerator");

        myFixture.checkResult("<?php\n" +
            "namespace App\\Entity;\n" +
            "\n" +
            "use App\\Entity\\CarRepository;\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "#[ORM\\Entity(repositoryClass: App\\Entity\\CarRepository::class)]\n" +
            "#[ORM\\Table(name: 'car')]\n" +
            "class Car\n" +
            "{\n" +
            "   #[Dummy]\n" +
            "   public $id;\n" +
            "}"
        );
    }
}
