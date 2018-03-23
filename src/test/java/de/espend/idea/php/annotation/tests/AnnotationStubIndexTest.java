package de.espend.idea.php.annotation.tests;

import de.espend.idea.php.annotation.AnnotationStubIndex;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationStubIndexTest extends AnnotationLightCodeInsightFixtureTestCase {

    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/fixtures";
    }

    public void testThatAnnotationClassIsInIndex() {
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Route");
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Foo\\RouteBar");
        assertIndexContains(AnnotationStubIndex.KEY, "My\\Annotations\\Foo\\RouteFoo");
    }
}
