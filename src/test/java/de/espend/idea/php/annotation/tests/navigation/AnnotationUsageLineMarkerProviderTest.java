package de.espend.idea.php.annotation.tests.navigation;

import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationUsageLineMarkerProviderTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("AnnotationUsageLineMarkerProvider.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testThatLineMarkerIsProvidedForAnnotationClass() {
        assertLineMarker(PhpPsiElementFactory.createPsiFileFromText(getProject(), "<?php\n" +
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
        ), new LineMarker.ToolTipEqualsAssert("Navigate to implementations"));
    }

    public void testThatNonAnnotationClassMustNotProvideLineMarker() {
        assertLineMarkerIsEmpty(PhpPsiElementFactory.createPsiFileFromText(getProject(), "<?php\n" +
            "namespace Doctrine\\ORM\\Mapping;\n" +
            "" +
            "{\n" +
            "   /**\n" +
            "   */\n" +
            "   class Embedded\n" +
            "   {\n" +
            "   }\n" +
            "}"
        ));
    }
}
