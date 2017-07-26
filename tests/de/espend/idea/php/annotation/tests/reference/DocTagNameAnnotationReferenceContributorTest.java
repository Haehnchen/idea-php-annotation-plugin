package de.espend.idea.php.annotation.tests.reference;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.codeInsight.PhpImportOptimizer;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.reference.DocTagNameAnnotationReferenceContributor
 */
public class DocTagNameAnnotationReferenceContributorTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testThatOptimizeImportShouldNotStripOurReferences() {
        String optimized = optimizeImports("<?php\n" +
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
        );

        assertTrue(optimized.contains("use FooBar\\Car as MyCar;"));
        assertFalse(optimized.contains("use FooBar\\Apple;"));
    }

    public void testThatOptimizeImportShouldNotStripOurReferencesInProperty() {
        String optimized = optimizeImports("<?php\n" +
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
            "  private $foo;\n" +
            "}\n"
        );

        assertTrue(optimized.contains("use FooBar\\Car;"));
    }

    public void testThatOptimizeImportShouldNotStripOurReferencesWithAlias() {
        String optimized = optimizeImports("<?php\n" +
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
        );

        assertTrue(optimized.contains("use FooBar\\Car as ORM;"));
        assertFalse(optimized.contains("use FooBar\\Apple;"));
    }

    public void testThatOptimizeImportShouldSupportAnnotationNamespaceOnlyByZend() {
        String optimized = optimizeImports("<?php\n" +
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
        );

        assertTrue(optimized.contains("use Zend\\Form\\Annotation;"));
    }

    public void testThatOptimizeImportShouldSupportStringConstants() {
        String[] strings = {
            "@Car(Foo::MY_CONST)",
            "@Car(name=Foo::MY_CONST)",
            "@Car(name={@Car(Foo::MY_CONST)})",
        };

        for (String string : strings) {
            String optimized = optimizeImports("<?php\n" +
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
            );

            assertTrue(optimized.contains("use MyConstant\\Foo;"));
        }
    }

    public void testThatOptimizeImportShouldRemoveNamespaceWithoutUse() {
        String optimized = optimizeImports("<?php\n" +
            "\n" +
            "namespace My;\n" +
            "\n" +
            "use FooBar\\Car;\n" +
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
        );

        assertFalse(optimized.contains("use MyConstant\\Foo;"));
    }

    public void testThatClassInterfaceIsSupportedForImportOptimization() {
        String optimized = optimizeImports("<?php\n" +
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
        );

        assertTrue(optimized.contains("use FooBar\\FoobarInterface;"));
        assertFalse(optimized.contains("use FooBar\\Apple;"));
    }

    @NotNull
    private String optimizeImports(@NotNull String content) {
        PsiFile psiFile = myFixture.configureByText(PhpFileType.INSTANCE, content);

        new WriteCommandAction(getProject()) {
            @Override
            protected void run(@NotNull Result result) throws Throwable {
                new PhpImportOptimizer().processFile(psiFile).run();
            }

            @Override
            public String getGroupID() {
                return "Optimize Imports";
            }
        }.execute();

        return psiFile.getText();
    }
}
