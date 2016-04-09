package de.espend.idea.php.annotation.tests.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationCompletionContributorTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testDocTagCompletionInClassScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* <caret>" +
                "*/" +
                "class Foo {}",
            "All", "Clazz"
        );

        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* <caret>" +
                "*/" +
                "class Foo {}",
            "Property"
        );
    }

    public void testDocTagCompletionInClassPropertyScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "class Foo {\n" +
                "  /**" +
                "   * <caret>" +
                "   */" +
                "  var $foo;" +
                "}",
            "Property", "All"
        );

        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "class Foo {\n" +
                "  /**" +
                "   * <caret>" +
                "   */" +
                "  var $foo;" +
                "}",
            "Clazz"
        );
    }

    public void testDocTagCompletionInClassMethodScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "class Foo {\n" +
                "  /**" +
                "   * <caret>" +
                "   */" +
                "  function foo() {}" +
                "}",
            "Method", "All"
        );

        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "class Foo {\n" +
                "  /**" +
                "   * <caret>" +
                "   */" +
                "  function foo() {}" +
                "}",
            "Property", "Clazz"
        );
    }

    public void testCompletionOfClassConstants() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;" +
                "use \\My\\Annotations\\Constants\n" +
                "/**\n" +
                "* @All(Constants::<caret>)\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "FOO"
        );
    }

    public void testThatAnnotationCompletionInsertUseAndClassNameWithRoundBracket() {
        assertCompletionResultEquals(PhpFileType.INSTANCE, "<?php\n" +
                "namespace {\n" +
                "  class Foo {\n" +
                "    /**\n" +
                "     * <caret>\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            "<?php\n" +
                "namespace {\n" +
                "\n" +
                "    use My\\Annotations\\All;\n" +
                "\n" +
                "    class Foo {\n" +
                "    /**\n" +
                "     * @All()\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            new LookupElementInsert.Assert() {
                @Override
                public boolean match(@NotNull LookupElement lookupElement) {
                    return "All".equals(lookupElement.getLookupString());
                }
            }
        );
    }

    public void testThatAnnotationCompletionInsertUseAndClassNameWithoutRoundBracket() {
        ApplicationSettings.getInstance().appendRoundBracket = false;

        assertCompletionResultEquals(PhpFileType.INSTANCE, "<?php\n" +
                "namespace {\n" +
                "  class Foo {\n" +
                "    /**\n" +
                "     * <caret>\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            "<?php\n" +
                "namespace {\n" +
                "\n" +
                "    use My\\Annotations\\All;\n" +
                "\n" +
                "    class Foo {\n" +
                "    /**\n" +
                "     * @All\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            new LookupElementInsert.Assert() {
                @Override
                public boolean match(@NotNull LookupElement lookupElement) {
                    return "All".equals(lookupElement.getLookupString());
                }
            }
        );
    }

    public void testThatAnnotationCompletionInsertUseAlias() {
        ApplicationSettings.getInstance().useAliasOptions = new ArrayList<UseAliasOption>();
        ApplicationSettings.getInstance().useAliasOptions.add(new UseAliasOption("My\\Annotations", "Bar"));

        assertCompletionResultEquals(PhpFileType.INSTANCE, "<?php\n" +
                "namespace {\n" +
                "  class Foo {\n" +
                "    /**\n" +
                "     * <caret>\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            "<?php\n" +
                "namespace {\n" +
                "\n" +
                "    use My\\Annotations as Bar;\n" +
                "\n" +
                "    class Foo {\n" +
                "    /**\n" +
                "     * @Bar\\All()\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            new LookupElementInsert.Assert() {
                @Override
                public boolean match(@NotNull LookupElement lookupElement) {
                    return "All".equals(lookupElement.getLookupString());
                }
            }
        );
    }
}
