package de.espend.idea.php.annotation.tests;

import de.espend.idea.php.annotation.AnnotationUsageIndex;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see de.espend.idea.php.annotation.AnnotationUsageIndex
 */
public class AnnotationUsageIndexTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
        myFixture.copyFileToProject("usages.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/fixtures";
    }

    public void testThatUsagesAreInIndex() {
        assertIndexContains(AnnotationUsageIndex.KEY, "Doctrine\\ORM\\Mapping\\Embedded");
    }
}
