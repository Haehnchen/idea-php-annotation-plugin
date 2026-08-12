package de.espend.idea.php.annotation.tests.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.ApplicationSettings.Companion.getInstance
import de.espend.idea.php.annotation.dict.UseAliasOption
import de.espend.idea.php.annotation.pattern.AnnotationPattern
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.completion.AnnotationCompletionContributor
 */
class AnnotationCompletionContributorTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/completion/fixtures"

    fun testDocTagCompletionInClassScope() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* <caret>" +
                    "*/" +
                    "class Foo {}",
            "All", "Clazz"
        )

        assertCompletionNotContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* <caret>" +
                    "*/" +
                    "class Foo {}",
            "Property"
        )
    }

    fun testCompletionForProperty() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @\\My\\Annotations\\All(\"a\",<caret>)" +
                    "*/" +
                    "class Foo {}",
            "strategy"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @\\My\\Annotations\\All(\"a\",<caret>)" +
                    "*/" +
                    "class Foo {}",
            "myPrivate"
        )
    }

    fun testCompletionForPropertyInsideAnnotationAttributes() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @\\My\\Annotations\\All(\"a\",<caret>)" +
                    "*/" +
                    "class Foo {}",
            "accessControl", "annotProperty", "attribute_blank_type", "attribute_no_type"
        )
    }

    fun testDocTagCompletionInClassPropertyScope() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "class Foo {\n" +
                    "  /**" +
                    "   * <caret>" +
                    "   */" +
                    "  var \$foo;" +
                    "}",
            "Property", "All"
        )

        assertCompletionNotContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "class Foo {\n" +
                    "  /**" +
                    "   * <caret>" +
                    "   */" +
                    "  var \$foo;" +
                    "}",
            "Clazz"
        )
    }

    fun testDocTagCompletionRendersDeprecatedClasses() {
        assertCompletionContainsDeprecationPresentation(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* <caret>" +
                    "*/" +
                    "class Foo {}",
            "ClazzDeprecated",
            true
        )

        assertCompletionContainsDeprecationPresentation(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* <caret>" +
                    "*/" +
                    "class Foo {}",
            "Clazz",
            false
        )
    }

    fun testDeprecatedClassesComeLastInCompletion() {
        myFixture.configureByText(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* <caret>" +
                    "*/" +
                    "class Foo {}"
        )

        myFixture.completeBasic()

        assertContainsOrdered(myFixture.lookupElementStrings ?: emptyList(), "Clazz", "AClazzDeprecated", "ClazzDeprecated")
    }

    fun testDocTagCompletionInClassMethodScope() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "class Foo {\n" +
                    "  /**" +
                    "   * <caret>" +
                    "   */" +
                    "  function foo() {}" +
                    "}",
            "Method", "All"
        )

        assertCompletionNotContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "class Foo {\n" +
                    "  /**" +
                    "   * <caret>" +
                    "   */" +
                    "  function foo() {}" +
                    "}",
            "Property", "Clazz"
        )
    }

    fun testCompletionOfClassConstants() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;" +
                    "use \\My\\Annotations\\Constants\n" +
                    "/**\n" +
                    "* @All(Constants::<caret>)\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "FOO"
        )
    }

    fun testCompletionOfClassConstantsWithNamespace() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**\n" +
                    "* @All(\\My\\Annotations\\Constants::<caret>)\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "FOO"
        )
    }

    fun testCompletionOfClassConstantsWithNamespaceAndUse() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use My\\Annotations;" +
                    "/**\n" +
                    "* @All(Annotations\\Constants::<caret>)\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "FOO"
        )
    }

    fun testCompletionOfClassConstantsInsideArray() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations\\All;" +
                    "use \\My\\Annotations\\Constants\n" +
                    "/**\n" +
                    "* @All(name={Constants::<caret>})\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "FOO"
        )
    }

    fun testThatAnnotationCompletionInsertUseAndClassNameWithRoundBracket() {
        assertCompletionResultEquals(
            PhpFileType.INSTANCE, "<?php\n" +
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
            LookupElementInsert.Assert { lookupElement: LookupElement? -> "All" == lookupElement!!.getLookupString() }
        )
    }

    fun testThatAnnotationCompletionInsertUseAndClassNameWithoutRoundBracket() {
        getInstance().appendRoundBracket = false

        assertCompletionResultEquals(
            PhpFileType.INSTANCE, "<?php\n" +
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
            LookupElementInsert.Assert { lookupElement: LookupElement? -> "All" == lookupElement!!.getLookupString() }
        )

        getInstance().appendRoundBracket = true
    }

    fun testThatAnnotationCompletionInsertUseAlias() {
        getInstance().useAliasOptions = ArrayList<UseAliasOption>()
        getInstance().useAliasOptions.add(UseAliasOption("My\\Annotations", "Bar", true))

        assertCompletionResultEquals(
            PhpFileType.INSTANCE, "<?php\n" +
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
            LookupElementInsert.Assert { lookupElement: LookupElement? -> "All" == lookupElement!!.getLookupString() }
        )

        getInstance().useAliasOptions = ArrayList<UseAliasOption>()
    }

    fun testThatDisabledUseAliasNotImported() {
        getInstance().useAliasOptions = ArrayList<UseAliasOption>()
        getInstance().useAliasOptions.add(UseAliasOption("My\\Annotations", "Bar", false))

        assertCompletionResultEquals(
            PhpFileType.INSTANCE, "<?php\n" +
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
            LookupElementInsert.Assert { lookupElement: LookupElement? -> "All" == lookupElement!!.getLookupString() }
        )

        getInstance().useAliasOptions = ArrayList<UseAliasOption>()
    }

    fun testCompletionOfAliasScope() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "use \\My\\Annotations as Foo" +
                    "/**\n" +
                    "* @Foo\\<caret>\n" +
                    "*/\n" +
                    "class Foo {}\n" +
                    "",
            "Foo\\All"
        )
    }

    /**
     * @see AnnotationPattern.getDocBlockTag
     */
    fun testDocTagCompletionInsideNestedPropertyValues() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @All(foo={@<caret>)" +
                    "*/" +
                    "class Foo {}",
            "All", "Clazz"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @All(foo = @<caret>)" +
                    "*/" +
                    "class Foo {}",
            "All", "Clazz"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @All(foo=@<caret>)" +
                    "*/" +
                    "class Foo {}",
            "All", "Clazz"
        )

        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @All(foo={@All(foo=@<caret>)})" +
                    "*/" +
                    "class Foo {}",
            "All", "Clazz"
        )
    }

    fun testDocTagCompletionInsideNestedPropertyValuesWithWhitespace() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @All(foo={@All(foo = @<caret>)})" +
                    "*/" +
                    "class Foo {}",
            "All", "Clazz"
        )
    }

    fun testDocTagInPropertyValueShouldNotComplete() {
        assertCompletionNotContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @All(foo=\"@<caret>\")" +
                    "*/" +
                    "class Foo {}",
            "All"
        )
    }

    fun testTheInternalAliasProvideCompletion() {
        assertCompletionContains(
            PhpFileType.INSTANCE, "<?php\n" +
                    "/**" +
                    "* @<caret>" +
                    "*/" +
                    "class Foo {}",
            "ORM\\Entity"
        )
    }

    fun testTheInternalAliasProvideCompletionAndImports() {
        assertCompletionResultEquals(
            PhpFileType.INSTANCE, "<?php\n" +
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
            LookupElementInsert.Assert { lookupElement: LookupElement? -> "ORM\\Entity" == lookupElement!!.getLookupString() }
        )
    }

    fun testTheInternalAliasProvideCompletionAndImportsWithAlreadyImported() {
        assertCompletionResultEquals(
            PhpFileType.INSTANCE, "<?php\n" +
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
            LookupElementInsert.Assert { lookupElement: LookupElement? -> "ORM\\Entity" == lookupElement!!.getLookupString() }
        )
    }

    fun testTheInternalAliasProvideCompletionAndImportsForAttributes() {
        assertCompletionResultEquals(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace {\n" +
                    "  class Foo {\n" +
                    "    #[<caret>]\n" +
                    "    function foo() {}\n" +
                    "  }\n" +
                    "}",
            "<?php\n" +
                    "namespace {\n" +
                    "\n" +
                    "    use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "\n" +
                    "    class Foo {\n" +
                    "    #[ORM\\Entity()]\n" +
                    "    function foo() {}\n" +
                    "  }\n" +
                    "}",
            LookupElementInsert.Assert { lookupElement: LookupElement? -> "ORM\\Entity" == lookupElement!!.getLookupString() }
        )
    }

    fun testTheImportedAliasProvideCompletionForAttributes() {
        assertCompletionResultEquals(
            PhpFileType.INSTANCE, "<?php\n" +
                    "namespace {\n" +
                    "\n" +
                    "    use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "\n" +
                    "    class Foo {\n" +
                    "    #[<caret>]\n" +
                    "    function foo() {}\n" +
                    "  }\n" +
                    "}",
            "<?php\n" +
                    "namespace {\n" +
                    "\n" +
                    "    use Doctrine\\ORM\\Mapping as ORM;\n" +
                    "\n" +
                    "    class Foo {\n" +
                    "    #[ORM\\Entity()]\n" +
                    "    function foo() {}\n" +
                    "  }\n" +
                    "}",
            LookupElementInsert.Assert { lookupElement: LookupElement? -> "ORM\\Entity" == lookupElement!!.getLookupString() }
        )
    }
}
