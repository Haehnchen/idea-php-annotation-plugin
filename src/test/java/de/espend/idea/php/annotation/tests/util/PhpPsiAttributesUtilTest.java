package de.espend.idea.php.annotation.tests.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import de.espend.idea.php.annotation.tests.AnnotationLightCodeInsightFixtureTestCase;
import de.espend.idea.php.annotation.util.PhpPsiAttributesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpPsiAttributesUtilTest extends AnnotationLightCodeInsightFixtureTestCase {
    public void testInsertNamedArgumentForAttributeWithColonAndNotAttribute() {
        PhpAttribute phpAttribute = createAttribute("Foobar()");

        WriteCommandAction.runWriteCommandAction(
            getProject(),
            () -> PhpPsiAttributesUtil.insertNamedArgumentForAttribute(getEditor(), phpAttribute, "foo", "Foobar::class")
        );

        PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(getEditor().getDocument());
        PsiDocumentManager.getInstance(getProject()).commitDocument(getEditor().getDocument());

        String text = phpAttribute.getText();
        assertTrue(text.contains("Foobar(foo: Foobar::class)"));
    }

    public void testInsertNamedArgumentForAttributeWithNoColon() {
        PhpAttribute phpAttribute = createAttribute("Foobar");

        WriteCommandAction.runWriteCommandAction(
            getProject(),
            () -> PhpPsiAttributesUtil.insertNamedArgumentForAttribute(getEditor(), phpAttribute, "foo", "Foobar::class")
        );

        PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(getEditor().getDocument());
        PsiDocumentManager.getInstance(getProject()).commitDocument(getEditor().getDocument());

        String text = phpAttribute.getText();
        assertTrue(text.contains("Foobar(foo: Foobar::class)"));
    }

    public void testInsertNamedArgumentForAttributeWithExistingNamedArgument() {
        PhpAttribute phpAttribute = createAttribute("Foobar(test: 'foobar')");

        WriteCommandAction.runWriteCommandAction(
            getProject(),
            () -> PhpPsiAttributesUtil.insertNamedArgumentForAttribute(getEditor(), phpAttribute, "foo", "Foobar::class")
        );

        PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(getEditor().getDocument());
        PsiDocumentManager.getInstance(getProject()).commitDocument(getEditor().getDocument());

        String text = phpAttribute.getText();
        assertTrue(text.contains("Foobar(test: 'foobar', foo: Foobar::class)"));
    }

    public void testInsertNamedArgumentForAttributeWithDefaultValue() {
        PhpAttribute phpAttribute = createAttribute("Foobar(\"Test\")");

        WriteCommandAction.runWriteCommandAction(
            getProject(),
            () -> PhpPsiAttributesUtil.insertNamedArgumentForAttribute(getEditor(), phpAttribute, "foo", "Foobar::class")
        );

        PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(getEditor().getDocument());
        PsiDocumentManager.getInstance(getProject()).commitDocument(getEditor().getDocument());

        String text = phpAttribute.getText();
        assertTrue(text.contains("Foobar(\"Test\", foo: Foobar::class)"));
    }

    private PhpAttribute createAttribute(@NotNull String attribute) {
        PsiFile file = myFixture.configureByText("test.php", "<?php\n" +
            "#["+ attribute +"]\n" +
            "class FooBar()\n"
        );

        return PsiTreeUtil.collectElementsOfType(file, PhpAttribute.class).iterator().next();
    }
}
