package de.espend.idea.php.annotation.tests.reference

import com.intellij.psi.PsiReference
import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase
import de.espend.idea.php.annotation.util.AnnotationUtil

class AnnotationPropertyValueReferenceContributorTest : AnnotationLightCodeInsightFixtureTestCase() {
    private val parameters = mutableListOf<AnnotationPropertyParameter>()

    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
        AnnotationUtil.EXTENSION_POINT_REFERENCES.point.registerExtension(
            PhpAnnotationReferenceProvider { property, _ ->
                parameters.add(property)
                PsiReference.EMPTY_ARRAY
            },
            testRootDisposable,
        )
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/completion/fixtures"

    fun testDelegatesNamedAnnotationPropertyToExtensions() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                use My\Annotations\All;

                /** @All(strategy="AU<caret>TO") */
                class Foo {}
            """.trimIndent(),
        )

        var element = myFixture.file.findElementAt(myFixture.caretOffset)
        while (element != null) {
            element.references
            element = element.parent
        }

        val property = parameters.firstOrNull { it.propertyName == "strategy" }
        assertNotNull(property)
        assertEquals(AnnotationPropertyParameter.Type.PROPERTY_VALUE, property!!.type)
        assertEquals("\\My\\Annotations\\All", property.phpClass.fqn)
    }
}
