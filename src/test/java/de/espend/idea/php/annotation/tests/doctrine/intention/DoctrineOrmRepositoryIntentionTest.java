package de.espend.idea.php.annotation.tests.doctrine.intention;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

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

    public void testThatRepositoryAnnotatorIsAvailable() {
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
            "Add Doctrine Repository"
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
            "Add Doctrine Repository"
        );
    }

}
