package de.espend.idea.php.annotation.tests.doctrine.action;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.action.DoctrineEmbeddedClassAnnotationGenerateAction
 */
public class DoctrineEmbeddedClassAnnotationGenerateActionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/action/fixtures";
    }

    public void testThatThatEmbeddableClassIsGeneratedForAnnotations() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "\n" +
            "class Foobar\n" +
            "{\n" +
            "<caret>" +
            "}"
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Embedded.ClassGenerator");

        myFixture.checkResult("<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\Embeddable\n" +
            " */\n" +
            "class Foobar\n" +
            "{\n" +
            "}"
        );
    }

    public void testThatThatEmbeddableClassIsGeneratedForAttributes() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "#[Foo]\n" +
            "class Foobar\n" +
            "{\n" +
            "<caret>" +
            "}"
        );

        myFixture.performEditorAction("PhpAnnotation.Doctrine.Embedded.ClassGenerator");

        myFixture.checkResult("<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "#[ORM\\Embeddable]\n" +
            "#[Foo]\n" +
            "class Foobar\n" +
            "{\n" +
            "}"
        );
    }
}
