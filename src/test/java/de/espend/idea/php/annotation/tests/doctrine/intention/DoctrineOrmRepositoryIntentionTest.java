package de.espend.idea.php.annotation.tests.doctrine.intention;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention
 */
public class DoctrineOrmRepositoryIntentionTest extends AnnotationLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/intention/fixtures";
    }

    public void testThatRepositoryAnnotatorIsAvailableForAnnotation() {
        assertIntentionIsAvailable(PhpFileType.INSTANCE, "<?php\n" +
                "\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\Ent<caret>ity()\n" +
                " */\n" +
                "class Relation\n" +
                "{\n" +
                "}",
            "Add Doctrine repository"
        );
    }

    public void testThatRepositoryAnnotatorIsAvailableForAttribute() {
        assertIntentionIsAvailable(PhpFileType.INSTANCE, "<?php\n" +
                "\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "#[ORM\\Ent<caret>ity()]" +
                "class Relation\n" +
                "{\n" +
                "}",
            "Add Doctrine repository"
        );

        assertIntentionIsAvailable(PhpFileType.INSTANCE, "<?php\n" +
                "\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "#[ORM\\Entity(\"fo<caret>o\")]" +
                "class Relation\n" +
                "{\n" +
                "}",
            "Add Doctrine repository"
        );
    }

    public void testThatRepositoryAnnotatorForAttributeValueIsAvailable() {
        assertIntentionIsAvailable(PhpFileType.INSTANCE,"<?php\n" +
                "\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\Entity(<caret>)\n" +
                " */\n" +
                "class Relation\n" +
                "{\n" +
                "}",
            "Add Doctrine repository"
        );
    }

    public void testAttribute() {
        myFixture.configureByText("test.php", "<?php\n" +
            "\n" +
            "namespace App\\Entity;\n" +
            "\n" +
            "use App\\Entity\\Repository\\UserRepository;\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "#[ORM\\Ent<caret>ity()]\n" +
            "class User {}\n"
        );

        String s = invokeAndGetText();

        assertTrue(s.contains("use App\\Entity\\Repository\\UserRepository;"));
        assertTrue(s.contains("#[ORM\\Entity(repositoryClass: UserRepository::class)]"));
    }

    public void testAnnotation() {
        myFixture.configureByText("test.php", "<?php\n" +
            "\n" +
            "namespace App\\Entity;\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "/**\n" +
            " * @ORM\\En<caret>tity()\n" +
            " */\n" +
            "class User {}\n"
        );

        String s = invokeAndGetText();

        assertTrue(s.contains("use App\\Entity\\Repository\\UserRepository;"));
        assertTrue(s.contains("@ORM\\Entity(repositoryClass=UserRepository::class)"));
    }

    private String invokeAndGetText() {
        PsiElement psiElement = myFixture.getFile().findElementAt(myFixture.getCaretOffset());

        WriteCommandAction.runWriteCommandAction(
            getProject(),
            () -> new DoctrineOrmRepositoryIntention().invoke(getProject(), getEditor(), psiElement)
        );

        return getEditor().getDocument().getText();
    }
}
