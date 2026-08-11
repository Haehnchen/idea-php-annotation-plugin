package de.espend.idea.php.annotation.tests.symfony

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class SymfonyCompletionProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/symfony/fixtures"

    fun testThatArrayCompletionForSymfonyRouteMethodsAreProvides() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
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
            "GET"
        )
    }

    fun testThatAttributesArrayCompletionForSymfonyRouteMethodsAreProvides() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "\n" +
                    "use Symfony\\Component\\Routing\\Annotation\\Route;\n" +
                    "\n" +
                    "class Test\n" +
                    "\n" +
                    "{\n" +
                    "  #[Route('/path', methods: ['<caret>'])]\n" +
                    "  public static function fooAction()\n" +
                    "  {\n" +
                    "  }\n" +
                    "}\n",
            "GET"
        )
    }
}
