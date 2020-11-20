package de.espend.idea.php.annotation.tests.annotation;

import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SyntaxErrorHighlightTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();

        myFixture.copyFileToProject("AnnotationClasses.php");
    }

    public String getTestDataPath() {
        return "src/test/java/de/espend/idea/php/annotation/tests/annotation/fixtures";
    }

    public void testValidAnnotation() {
        assertSyntaxViolationHighlighting(this.loadFileContent("ValidAnnotation.php"), 0);
    }

    public void testValidMultilineStringAnnotation() {
        assertSyntaxViolationHighlighting(this.loadFileContent("ValidMultilineStringAnnotation.php"), 0);
    }

    public void testNoContentAnnotation() {
        assertSyntaxViolationHighlighting(this.loadFileContent("NoContentAnnotation.php"), 0);
    }

    public void testValidComplexAnnotation() {
        assertSyntaxViolationHighlighting(this.loadFileContent("ValidComplexAnnotation.php"), 0);
    }

    public void testMissingComma() {
        assertSyntaxViolationHighlighting(this.loadFileContent("MissingComma.php"), 1);
    }

    public void testMissingQuotationMark() {
        assertSyntaxViolationHighlighting(this.loadFileContent("MissingQuotationMark.php"), 2);
    }

    public void testMissingCurlyBracket() {
        assertSyntaxViolationHighlighting(this.loadFileContent("MissingCurlyBracket.php"), 2);
    }

    public void testMissingRoundBracket() {
        assertSyntaxViolationHighlighting(this.loadFileContent("MissingRoundBracket.php"), 1);
    }

    public void testMultipleErrors() {
        assertSyntaxViolationHighlighting(this.loadFileContent("MultipleErrors.php"), 3);
    }

    public void testMissingEquals() {
        assertSyntaxViolationHighlighting(this.loadFileContent("MissingEquals.php"), 2);
    }

    private String loadFileContent(String relativePath) {
        String path = this.getTestDataPath() + "/" + relativePath;

        try {
            FileInputStream fis = new FileInputStream(path);
            return IOUtils.toString(fis, StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        return "";
    }
}


