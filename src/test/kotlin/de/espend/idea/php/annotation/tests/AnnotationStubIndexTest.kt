package de.espend.idea.php.annotation.tests

import de.espend.idea.php.annotation.AnnotationStubIndex
import de.espend.idea.php.annotation.dict.AnnotationTarget
import de.espend.idea.php.annotation.util.AnnotationUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationStubIndexTest : AnnotationLightCodeInsightFixtureTestCase() {
    public override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("classes.php")
        myFixture.copyFileToProject("classes_targets.php")
    }

    override fun getTestDataPath(): String {
        return "src/test/java/de/espend/idea/php/annotation/tests/fixtures"
    }

    fun testThatAnnotationClassIsInIndex() {
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Route")
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Foo\\RouteBar")
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Foo\\RouteFoo")
    }

    fun testThatAnnotationTargetsAreStoredInIndex() {
        assertIndexContainsKeyWithValue(AnnotationStubIndex.KEY, "My\\Annotations\\PropertyOnly") { value ->
            AnnotationUtil.getAnnotationTargetsFromSerializedValue(value).contains(AnnotationTarget.PROPERTY)
        }

        assertIndexContainsKeyWithValue(AnnotationStubIndex.KEY, "My\\Annotations\\MethodAndAll") { value ->
            val targets = AnnotationUtil.getAnnotationTargetsFromSerializedValue(value)
            targets.contains(AnnotationTarget.METHOD) && targets.contains(AnnotationTarget.ALL)
        }
    }
}
