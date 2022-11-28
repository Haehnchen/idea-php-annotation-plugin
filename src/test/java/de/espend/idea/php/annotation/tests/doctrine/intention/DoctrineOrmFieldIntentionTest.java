package de.espend.idea.php.annotation.tests.doctrine.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmFieldIntention
 */
public class DoctrineOrmFieldIntentionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/intention/fixtures";
    }
    public void testThatAddDoctrineColumnIsAvailable() {
        assertIntentionIsAvailable(PhpFileType.INSTANCE, "<?php\n" +
                "\n" +
                "class Foobar\n" +
                "{\n" +
                "   public $i<caret>d;" +
                "}",
            "Add Doctrine column"
        );
    }

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResult() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "\n" +
            "class Foobar\n" +
            "{\n" +
            "   public $i<caret>d;\n" +
            "}");

        final IntentionAction action = myFixture.findSingleIntention("Add Doctrine column");
        myFixture.launchAction(action);

        myFixture.checkResult("<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "class Foobar\n" +
            "{\n" +
            "    /**\n" +
            "     * @ORM\\Id\n" +
            "     * @ORM\\GeneratedValue(strategy=\"AUTO\")\n" +
            "     * @ORM\\Column(type=\"integer\")\n" +
            "     */public $id;\n" +
            "}"
        );
    }

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributes() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "\n" +
            "#[Foo]\n" +
            "class Foobar\n" +
            "{\n" +
            "   public $i<caret>d;\n" +
            "}");

        final IntentionAction action = myFixture.findSingleIntention("Add Doctrine column");
        myFixture.launchAction(action);

        myFixture.checkResult("<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "#[Foo]\n" +
            "class Foobar\n" +
            "{\n" +
            "    #[ORM\\Id]\n" +
            "    #[ORM\\GeneratedValue(strategy: 'AUTO')]\n" +
            "    #[ORM\\Column(type: 'integer')]\n" +
            "    public $id;\n" +
            "}"
        );
    }

    public void testThatAddDoctrineColumnIsAvailableIsInvokedWithResultForAttributesForExisting() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
            "\n" +
            "class Foobar\n" +
            "{\n" +
            "    #[ORM\\GeneratedValue(strategy: 'AUTO')]\n" +
            "    public $i<caret>d;\n" +
            "}");

        final IntentionAction action = myFixture.findSingleIntention("Add Doctrine column");
        myFixture.launchAction(action);

        myFixture.checkResult("<?php\n" +
            "\n" +
            "use Doctrine\\ORM\\Mapping as ORM;\n" +
            "\n" +
            "class Foobar\n" +
            "{\n" +
            "    #[ORM\\Id]\n" +
            "    #[ORM\\Column(type: 'integer')]\n" +
            "    #[ORM\\GeneratedValue(strategy: 'AUTO')]\n" +
            "    public $id;\n" +
            "}"
        );
    }
}
