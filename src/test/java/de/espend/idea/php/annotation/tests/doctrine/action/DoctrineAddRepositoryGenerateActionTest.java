package de.espend.idea.php.annotation.tests.doctrine.action;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrineAddRepositoryGenerateAction
 */
public class DoctrineAddRepositoryGenerateActionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("DoctrineAddRepositoryGenerateAction.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/action/fixtures";
    }

    public void testGenerationForAnnotation() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "" +
            "namespace App\\Entity;" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Entity()\n" +
            " */\n" +
            "class User\n" +
            "{" +
            "<caret>\n" +
            "}"
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.DoctrineAddRepositoryGenerateAction");

        myFixture.checkResult("<?php\n" +
            "namespace App\\Entity;\n" +
            "use App\\Entity\\Repository\\UserRepository;\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Entity(repositoryClass=UserRepository::class)\n" +
            " */\n" +
            "class User\n" +
            "{\n" +
            "}"
        );
    }

    public void testGenerationForAnnotationInsideDocCommentScope() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "" +
            "namespace App\\Entity;" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Ent<caret>ity()\n" +
            " */\n" +
            "class User\n" +
            "{" +
            "\n" +
            "}"
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Orm.DoctrineAddRepositoryGenerateAction");

        myFixture.checkResult("<?php\n" +
            "namespace App\\Entity;\n" +
            "use App\\Entity\\Repository\\UserRepository;\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Entity(repositoryClass=UserRepository::class)\n" +
            " */\n" +
            "class User\n" +
            "{\n" +
            "}"
        );
    }
}
