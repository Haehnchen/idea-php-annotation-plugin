package de.espend.idea.php.annotation.tests.annotator

import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase
import de.espend.idea.php.annotation.util.AnnotationUtil

class AnnotationDocTagAnnotatorTest : AnnotationLightCodeInsightFixtureTestCase() {
    private val parameters = mutableListOf<PhpAnnotationDocTagAnnotatorParameter>()

    public override fun setUp() {
        super.setUp()
        AnnotationUtil.EP_DOC_TAG_ANNOTATOR.point.registerExtension(
            PhpAnnotationDocTagAnnotator { parameters.add(it) },
            testRootDisposable,
        )
    }

    fun testDelegatesResolvedAnnotationClass() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App;

                class Entity {}

                /** @Entity */
                class Foo {}
            """.trimIndent(),
        )

        myFixture.doHighlighting()

        val parameter = parameters.firstOrNull { it.phpDocTag.name == "@Entity" }
        assertNotNull(parameter)
        assertEquals("\\App\\Entity", parameter!!.annotationClass?.fqn)
    }

    fun testDelegatesUnresolvedAnnotationWithoutClass() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App;

                /** @Unknown */
                class Foo {}
            """.trimIndent(),
        )

        myFixture.doHighlighting()

        val parameter = parameters.firstOrNull { it.phpDocTag.name == "@Unknown" }
        assertNotNull(parameter)
        assertNull(parameter!!.annotationClass)
    }

    fun testDoesNotDelegateBlockedDocTag() {
        myFixture.configureByText(
            PhpFileType.INSTANCE,
            """
                <?php

                /** @param string ${'$'}value */
                function foo(string ${'$'}value): void {}
            """.trimIndent(),
        )

        myFixture.doHighlighting()

        assertFalse(parameters.any { it.phpDocTag.name == "@param" })
    }
}
