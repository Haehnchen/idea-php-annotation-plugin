package de.espend.idea.php.annotation.tests.navigation

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class ColumnNameCompletionProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("ColumnNameCompletionProvider.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/navigation/fixtures"

    fun testThatDoctrineORMColumnNameCompletionForAnnotation() {
        assertCompletionContains(
            "test.php", "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "\n" +
                    "/**\n" +
                    " * @ORM\\Column(type=\"json<caret>\")\n" +
                    " */\n" +
                    "class Foo\n" +
                    "{\n" +
                    "    /**\n" +
                    "    * @ORM\\Column(name=\"<caret>\")\n" +
                    "    */\n" +
                    "    public \$createdAt;\n" +
                    "}",
            "created_at"
        )
    }

    fun testThatDoctrineORMColumnNameCompletionForAttribute() {
        assertCompletionContains(
            "test.php", "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{" +
                    "   #[ORM\\Column(name: '<caret>')]\n" +
                    "   private int \$attributeId;" +
                    "}",
            "attribute_id"
        )

        assertCompletionContains(
            "test.php", "<?php\n" +
                    "use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{" +
                    "    public function __construct(\n" +
                    "        #[ORM\\Column(name: '<caret>')]\n" +
                    "        private readonly int \$attributeId,\n" +
                    "    )\n" +
                    "    {" +
                    "}",
            "attribute_id"
        )
    }
}
