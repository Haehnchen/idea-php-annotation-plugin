package de.espend.idea.php.annotation.tests.navigation

import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase.LineMarker.ToolTipEqualsAssert

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationUsageLineMarkerProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("AnnotationUsageLineMarkerProvider.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/navigation/fixtures"

    fun testThatLineMarkerIsProvidedForAnnotationClass() {
        assertLineMarker(
            PhpPsiElementFactory.createPsiFileFromText(
                getProject(), "<?php\n" +
                        "namespace Doctrine\\ORM\\Mapping;\n" +
                        "" +
                        "{\n" +
                        "   /**\n" +
                        "   * @Annotation\n" +
                        "   */\n" +
                        "   class Embedded\n" +
                        "   {\n" +
                        "   }\n" +
                        "}"
            ), ToolTipEqualsAssert("Navigate to implementations")
        )
    }

    fun testThatLineMarkerIsProvidedForAttributeClass() {
        assertLineMarker(
            PhpPsiElementFactory.createPsiFileFromText(
                getProject(), "<?php\n" +
                        "namespace Doctrine\\ORM\\Mapping;\n" +
                        "" +
                        "{\n" +
                        "   #[\\Attribute]\n" +
                        "   class Embedded\n" +
                        "   {\n" +
                        "   }\n" +
                        "}"
            ), ToolTipEqualsAssert("Navigate to implementations")
        )
    }

    fun testThatNonAnnotationClassMustNotProvideLineMarker() {
        assertLineMarkerIsEmpty(
            PhpPsiElementFactory.createPsiFileFromText(
                getProject(), "<?php\n" +
                        "namespace Doctrine\\ORM\\Mapping;\n" +
                        "" +
                        "{\n" +
                        "   /**\n" +
                        "   */\n" +
                        "   class Embedded\n" +
                        "   {\n" +
                        "   }\n" +
                        "}"
            )
        )
    }

    fun testThatNonAnnotationClassDoesNotAbortFollowingAnnotationClass() {
        assertLineMarker(
            PhpPsiElementFactory.createPsiFileFromText(
                getProject(), "<?php\n" +
                        "namespace Doctrine\\ORM\\Mapping;\n" +
                        "" +
                        "{\n" +
                        "   class PlainClass\n" +
                        "   {\n" +
                        "   }\n" +
                        "\n" +
                        "   /**\n" +
                        "   * @Annotation\n" +
                        "   */\n" +
                        "   class Embedded\n" +
                        "   {\n" +
                        "   }\n" +
                        "}"
            ), ToolTipEqualsAssert("Navigate to implementations")
        )
    }
}
