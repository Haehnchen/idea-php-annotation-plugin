package de.espend.idea.php.annotation.tests.doctrine.reference;

import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineCustomIdGeneratorTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("classes.php");
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testThatDoctrineCustomIdGeneratorPropertyProvidesClassReferences() {
        assertCompletionContains(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "    /** @ORM\\CustomIdGenerator(class=\"<caret>\") */\n" +
                "    protected $logo;\n" +
                "}",
            "Bar"
        );

        assertReferenceMatchOnParent(PhpFileType.INSTANCE, "<?php\n" +
                "use Doctrine\\ORM\\Mapping as ORM;\n" +
                "class Foo\n" +
                "{\n" +
                "    /** @ORM\\CustomIdGenerator(class=\"My\\FooC<caret>lass\\Bar\") */\n" +
                "    protected $logo;\n" +
                "}",
            PlatformPatterns.psiElement(PhpClass.class)
        );
    }
}
