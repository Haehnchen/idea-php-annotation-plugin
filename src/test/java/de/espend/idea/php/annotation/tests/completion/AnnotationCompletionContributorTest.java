package de.espend.idea.php.annotation.tests.completion;

import com.jetbrains.php.lang.PhpFileType;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.util.ArrayList;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.completion.AnnotationCompletionContributor
 */
public class AnnotationCompletionContributorTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/completion/fixtures";
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

    public void testCompletionForProperty() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @\\My\\Annotations\\All(\"a\",<caret>)" +
                "*/" +
                "class Foo {}",
            "strategy"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @\\My\\Annotations\\All(\"a\",<caret>)" +
                "*/" +
                "class Foo {}",
            "myPrivate"
        );
    }

    public void testCompletionForPropertyInsideAnnotationAttributes() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @\\My\\Annotations\\All(\"a\",<caret>)" +
                "*/" +
                "class Foo {}",
            "accessControl", "annotProperty", "attribute_blank_type", "attribute_no_type"
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

    public void testDocTagCompletionRendersDeprecatedClasses() {
        assertCompletionContainsDeprecationPresentation(PhpFileType.INSTANCE, "<?php\n" +
                        "/**" +
                        "* <caret>" +
                        "*/" +
                        "class Foo {}",
                "ClazzDeprecated",
                true
        );

        assertCompletionContainsDeprecationPresentation(PhpFileType.INSTANCE, "<?php\n" +
                        "/**" +
                        "* <caret>" +
                        "*/" +
                        "class Foo {}",
                "Clazz",
                false
        );
    }

    public void testDeprecatedClassesComeLastInCompletion() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php\n" +
                        "/**" +
                        "* <caret>" +
                        "*/" +
                        "class Foo {}"
        );

        myFixture.completeBasic();

        assertContainsOrdered(myFixture.getLookupElementStrings(), "Clazz", "AClazzDeprecated", "ClazzDeprecated");
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

    public void testCompletionOfClassConstantsWithNamespace() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**\n" +
                "* @All(\\My\\Annotations\\Constants::<caret>)\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "FOO"
        );
    }

    public void testCompletionOfClassConstantsWithNamespaceAndUse() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use My\\Annotations;" +
                "/**\n" +
                "* @All(Annotations\\Constants::<caret>)\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "FOO"
        );
    }

    public void testCompletionOfClassConstantsInsideArray() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations\\All;" +
                "use \\My\\Annotations\\Constants\n" +
                "/**\n" +
                "* @All(name={Constants::<caret>})\n" +
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
            lookupElement -> "All".equals(lookupElement.getLookupString())
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
            lookupElement -> "All".equals(lookupElement.getLookupString())
        );

        ApplicationSettings.getInstance().appendRoundBracket = true;
    }

    public void testThatAnnotationCompletionInsertUseAlias() {
        ApplicationSettings.getInstance().useAliasOptions = new ArrayList<>();
        ApplicationSettings.getInstance().useAliasOptions.add(new UseAliasOption("My\\Annotations", "Bar", true));

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
            lookupElement -> "All".equals(lookupElement.getLookupString())
        );

        ApplicationSettings.getInstance().useAliasOptions = new ArrayList<>();
    }

    public void testThatDisabledUseAliasNotImported() {
        ApplicationSettings.getInstance().useAliasOptions = new ArrayList<>();
        ApplicationSettings.getInstance().useAliasOptions.add(new UseAliasOption("My\\Annotations", "Bar", false));

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
            lookupElement -> "All".equals(lookupElement.getLookupString())
        );

        ApplicationSettings.getInstance().useAliasOptions = new ArrayList<>();
    }

    public void testCompletionOfAliasScope() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use \\My\\Annotations as Foo" +
                "/**\n" +
                "* @Foo\\<caret>\n" +
                "*/\n" +
                "class Foo {}\n" +
                "",
            "Foo\\All"
        );
    }

    /**
     * @see AnnotationPattern#getDocBlockTag()
     */
    public void testDocTagCompletionInsideNestedPropertyValues() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @All(foo={@<caret>)" +
                "*/" +
                "class Foo {}",
            "All", "Clazz"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @All(foo = @<caret>)" +
                "*/" +
                "class Foo {}",
            "All", "Clazz"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @All(foo=@<caret>)" +
                "*/" +
                "class Foo {}",
            "All", "Clazz"
        );

        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @All(foo={@All(foo=@<caret>)})" +
                "*/" +
                "class Foo {}",
            "All", "Clazz"
        );
    }

    public void testDocTagCompletionInsideNestedPropertyValuesWithWhitespace() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @All(foo={@All(foo = @<caret>)})" +
                "*/" +
                "class Foo {}",
            "All", "Clazz"
        );
    }

    public void testDocTagInPropertyValueShouldNotComplete() {
        assertCompletionNotContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @All(foo=\"@<caret>\")" +
                "*/" +
                "class Foo {}",
            "All"
        );
    }

    public void testTheInternalAliasProvideCompletion() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "/**" +
                "* @<caret>" +
                "*/" +
                "class Foo {}",
            "ORM\\Entity"
        );
    }

    public void testTheInternalAliasProvideCompletionAndImports() {
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
                "    use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "    class Foo {\n" +
                "    /**\n" +
                "     * @ORM\\Entity()\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            lookupElement -> "ORM\\Entity".equals(lookupElement.getLookupString())
        );
    }

    public void testTheInternalAliasProvideCompletionAndImportsWithAlreadyImported() {
        assertCompletionResultEquals(PhpFileType.INSTANCE, "<?php\n" +
                "namespace {\n" +
                "\n" +
                "    use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "    class Foo {\n" +
                "    /**\n" +
                "     * <caret>\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            "<?php\n" +
                "namespace {\n" +
                "\n" +
                "    use Doctrine\\ORM\\Mapping as ORM;\n" +
                "\n" +
                "    class Foo {\n" +
                "    /**\n" +
                "     * @ORM\\Entity()\n" +
                "     */\n" +
                "    function foo() {}\n" +
                "  }\n" +
                "}",
            lookupElement -> "ORM\\Entity".equals(lookupElement.getLookupString())
        );
    }
}
