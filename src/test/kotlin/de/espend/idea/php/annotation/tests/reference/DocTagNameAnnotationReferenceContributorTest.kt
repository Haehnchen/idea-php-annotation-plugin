package de.espend.idea.php.annotation.tests.reference

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiFile
import com.jetbrains.php.codeInsight.PhpImportOptimizer
import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.reference.DocTagNameAnnotationReferenceContributor
 */
class DocTagNameAnnotationReferenceContributorTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/java/de/espend/idea/php/annotation/tests/reference/fixtures"

    fun testThatOptimizeImportShouldNotStripOurReferences() {
        val optimized = optimizeImports(
            "<?php\n" +
                    "\n" +
                    "namespace My;\n" +
                    "\n" +
                    "use FooBar\\Car as MyCar;\n" +
                    "use FooBar\\Apple;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /**\n" +
                    "   * @MyCar\\SubCar\n" +
                    "   */\n" +
                    "  public function foo()\n" +
                    "  {\n" +
                    "  }\n" +
                    "}\n"
        )

        assertTrue(optimized.contains("use FooBar\\Car as MyCar;"))
        assertFalse(optimized.contains("use FooBar\\Apple;"))
    }

    fun testThatOptimizeImportShouldNotStripOurReferencesInProperty() {
        val optimized = optimizeImports(
            "<?php\n" +
                    "\n" +
                    "namespace My;\n" +
                    "\n" +
                    "use FooBar\\Car;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /**\n" +
                    "   * @Car\n" +
                    "   */\n" +
                    "  private \$foo;\n" +
                    "}\n"
        )

        assertTrue(optimized.contains("use FooBar\\Car;"))
    }

    fun testThatOptimizeImportShouldNotStripOurReferencesWithAlias() {
        val optimized = optimizeImports(
            "<?php\n" +
                    "\n" +
                    "namespace My;\n" +
                    "\n" +
                    "use FooBar\\Car as ORM;\n" +
                    "use FooBar\\Apple;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /**\n" +
                    "   * @ORM\n" +
                    "   */\n" +
                    "  public function foo()\n" +
                    "  {\n" +
                    "  }\n" +
                    "}\n"
        )

        assertTrue(optimized.contains("use FooBar\\Car as ORM;"))
        assertFalse(optimized.contains("use FooBar\\Apple;"))
    }

    fun testThatOptimizeImportShouldSupportAnnotationNamespaceOnlyByZend() {
        val optimized = optimizeImports(
            "<?php\n" +
                    "\n" +
                    "namespace My;\n" +
                    "\n" +
                    "use Zend\\Form\\Annotation;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /**\n" +
                    "   * @Annotation\\Exclude\n" +
                    "   */\n" +
                    "  public function foo()\n" +
                    "  {\n" +
                    "  }\n" +
                    "}\n"
        )

        assertTrue(optimized.contains("use Zend\\Form\\Annotation;"))
    }

    fun testThatOptimizeImportShouldSupportStringConstants() {
        val strings = arrayOf<String?>(
            "@Car(Foo::MY_CONST)",
            "@Car(name=Foo::MY_CONST)",
            "@Car(name={@Car(Foo::MY_CONST)})",
        )

        for (string in strings) {
            val optimized = optimizeImports(
                "<?php\n" +
                        "\n" +
                        "namespace My;\n" +
                        "\n" +
                        "use FooBar\\Car;\n" +
                        "use MyConstant\\Foo;\n" +
                        "\n" +
                        "class Foo\n" +
                        "{\n" +
                        "  /**\n" +
                        "   * " + string +
                        "   */\n" +
                        "  public function foo()\n" +
                        "  {\n" +
                        "  }\n" +
                        "}\n"
            )

            assertTrue(optimized.contains("use MyConstant\\Foo;"))
        }
    }

    fun testThatOptimizeImportShouldRemoveNamespaceWithoutUse() {
        val optimized = optimizeImports(
            "<?php\n" +
                    "\n" +
                    "namespace My;\n" +
                    "\n" +
                    "use FooBar\\Car;\n" +
                    "use FooBar\\Apple;\n" +
                    "use MyConstant\\Foo;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /**\n" +
                    "   * @Car(Foo\\Foo::MY_CONST)" +
                    "   */\n" +
                    "  public function foo()\n" +
                    "  {\n" +
                    "  }\n" +
                    "}\n"
        )

        assertFalse(optimized.contains("use FooBar\\Apple;"))
        assertTrue(optimized.contains("use FooBar\\Car;"))
        assertTrue(optimized.contains("use MyConstant\\Foo;"))
    }

    fun testThatClassInterfaceIsSupportedForImportOptimization() {
        val optimized = optimizeImports(
            "<?php\n" +
                    "\n" +
                    "namespace My;\n" +
                    "\n" +
                    "use FooBar\\FoobarInterface;\n" +
                    "use FooBar\\Apple;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /**\n" +
                    "   * @Car(FoobarInterface::class)" +
                    "   */\n" +
                    "  public function foo()\n" +
                    "  {\n" +
                    "  }\n" +
                    "}\n"
        )

        assertTrue(optimized.contains("use FooBar\\FoobarInterface;"))
        assertFalse(optimized.contains("use FooBar\\Apple;"))
    }

    fun testThatClassConstantWithNamespaceMustNotBeRemoved() {
        val optimized = optimizeImports(
            "<?php\n" +
                    "\n" +
                    "namespace My;\n" +
                    "\n" +
                    "use FooBar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /**\n" +
                    "   * @Car(FooBar\\FoobarInterface::class)" +
                    "   */\n" +
                    "  public function foo()\n" +
                    "  {\n" +
                    "  }\n" +
                    "}\n"
        )

        assertTrue(optimized.contains("use FooBar;"))
    }

    fun testThatConstantWithNamespaceMustNotBeRemoved() {
        val optimized = optimizeImports(
            "<?php\n" +
                    "\n" +
                    "namespace My;\n" +
                    "\n" +
                    "use FooBar;\n" +
                    "\n" +
                    "class Foo\n" +
                    "{\n" +
                    "  /**\n" +
                    "   * @Car(FooBar\\FoobarInterface::TEST)" +
                    "   */\n" +
                    "  public function foo()\n" +
                    "  {\n" +
                    "  }\n" +
                    "}\n"
        )

        assertTrue(optimized.contains("use FooBar;"))
    }

    fun testPhpDocTagsShouldNotBindToVariables() {
        myFixture.configureByText(
            "foo.php", "<?php\n" +
                    "/** @var \$myVar string */\n" +
                    "\$va<caret>r = 'foo';"
        )

        myFixture.renameElement(myFixture.getElementAtCaret(), "bar")

        myFixture.checkResult(
            "<?php\n" +
                    "/** @var \$myVar string */\n" +
                    "\$ba<caret>r = 'foo';"
        )
    }

    private fun optimizeImports(content: String): String {
        val psiFile: PsiFile = myFixture.configureByText(PhpFileType.INSTANCE, content)
        WriteCommandAction.runWriteCommandAction(getProject(), { PhpImportOptimizer().processFile(psiFile).run() })
        return psiFile.getText()
    }
}
