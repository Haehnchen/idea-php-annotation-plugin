package de.espend.idea.php.annotation.tests

import de.espend.idea.php.annotation.AnnotationUsageIndex

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.AnnotationUsageIndex
 */
class AnnotationUsageIndexTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
        myFixture.copyFileToProject("usages.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/java/de/espend/idea/php/annotation/tests/fixtures"
    }

    fun testThatUsagesAreInIndex() {
        assertIndexContains(AnnotationUsageIndex.KEY, "Doctrine\\ORM\\Mapping\\Embedded")
        assertIndexContains(AnnotationUsageIndex.KEY, "My\\Route")
    }
}
