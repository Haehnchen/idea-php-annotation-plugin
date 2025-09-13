package de.espend.idea.php.annotation.tests.doctrine.util;

import com.intellij.codeInsight.lookup.LookupElement;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.util.Collection;

public class DoctrineUtilTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("Type.php");
        myFixture.copyFileToProject("Types.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/doctrine/util/fixtures";
    }

    public void testGetTypes() {
        Collection<LookupElement> types = DoctrineUtil.getTypes(getProject());

        // static
        assertTrue(types.stream().anyMatch(lookupElement -> "id".equals(lookupElement.getLookupString())));
        assertTrue(types.stream().anyMatch(lookupElement -> "array".equals(lookupElement.getLookupString())));

        // Types
        assertTrue(types.stream().anyMatch(lookupElement -> "datetime_immutable".equals(lookupElement.getLookupString())));

        // Type by name
        assertTrue(types.stream().anyMatch(lookupElement -> "my_type".equals(lookupElement.getLookupString())));
    }
}
