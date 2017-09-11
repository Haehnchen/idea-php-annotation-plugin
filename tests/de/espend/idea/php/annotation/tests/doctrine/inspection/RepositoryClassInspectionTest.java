package de.espend.idea.php.annotation.tests.doctrine.inspection;

import de.espend.idea.php.annotation.doctrine.inspection.RepositoryClassInspection;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.doctrine.inspection.RepositoryClassInspection
 */
public class RepositoryClassInspectionTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testFoo() {
        assertLocalInspectionContains("test.php", "<?php\n" +
                "\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "/**\n" +
                " * @ORM\\En<caret>tity(repositoryClass=\"Foobar\")\n" +
                " */\n" +
                "class Foo\n" +
                "{\n" +
                "}",
            RepositoryClassInspection.MESSAGE
        );
    }
}
