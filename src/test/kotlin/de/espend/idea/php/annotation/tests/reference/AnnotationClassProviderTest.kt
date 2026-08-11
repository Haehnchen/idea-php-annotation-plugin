package de.espend.idea.php.annotation.tests.reference

import com.intellij.patterns.PlatformPatterns
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase

class AnnotationClassProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
    }

    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/completion/fixtures"

    fun testTargetEntityProvidesClassReference() {
        assertReferenceMatchOnParent(
            PhpFileType.INSTANCE,
            """
                <?php
                namespace App;

                use My\Annotations\All;

                class Target {}

                /** @All(targetEntity="\App\Tar<caret>get") */
                class Foo {}
            """.trimIndent(),
            PlatformPatterns.psiElement(PhpClass::class.java).withName("Target"),
        )
    }
}
