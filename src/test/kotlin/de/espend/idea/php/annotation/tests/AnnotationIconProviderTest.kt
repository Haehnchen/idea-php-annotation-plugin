package de.espend.idea.php.annotation.tests

import com.intellij.util.PlatformIcons
import com.jetbrains.php.lang.PhpFileType
import de.espend.idea.php.annotation.AnnotationIconProvider

class AnnotationIconProviderTest : AnnotationLightCodeInsightFixtureTestCase() {
    override fun getTestDataPath(): String =
        "src/test/kotlin/de/espend/idea/php/annotation/tests/fixtures"

    fun testProvidesAnnotationIconForAnnotationFile() {
        val file = myFixture.configureByFile("AnnotationIconProvider.php")

        assertSame(PlatformIcons.ANNOTATION_TYPE_ICON, AnnotationIconProvider().getIcon(file, 0))
    }

    fun testDoesNotProvideIconForRegularPhpFile() {
        val file = myFixture.configureByText(PhpFileType.INSTANCE, "<?php class Foo {}")

        assertNull(AnnotationIconProvider().getIcon(file, 0))
    }
}
