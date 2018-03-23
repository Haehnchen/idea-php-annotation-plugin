package de.espend.idea.php.annotation.tests.symfony;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SymfonyCompletionProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/symfony/fixtures";
    }

    public void testThatArrayCompletionForSymfonyRouteMethodsAreProvides() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "\n" +
                "use Symfony\\Component\\Routing\\Annotation\\Route;\n" +
                "\n" +
                "class Test\n" +
                "{\n" +
                "  /**\n" +
                "   * @Route(methods={\"CONNECT\", \"<caret>\"})\n" +
                "   */\n" +
                "  public static function fooAction()\n" +
                "  {\n" +
                "  }\n" +
                "}\n",
            "GET");
    }
}
