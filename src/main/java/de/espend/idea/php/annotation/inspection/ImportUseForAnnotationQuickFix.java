package de.espend.idea.php.annotation.inspection;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBList;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ImportUseForAnnotationQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
    @NotNull
    private final Collection<String> classes;

    ImportUseForAnnotationQuickFix(@NotNull PhpDocTag phpDocTag, @NotNull Collection<String> classes) {
        super(phpDocTag);
        this.classes = classes;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Annotation";
    }

    @NotNull
    @Override
    public String getText() {
        // single class presentable item
        if(this.classes.size() == 1) {
            return "Import: " + this.classes.iterator().next();
        }

        // multiple classes found just display overall count
        return String.format("Import class (%s)", this.classes.size());
    }

    public void invoke(@NotNull Project project, @NotNull PsiFile psiFile, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        if(editor == null) {
            return;
        }

        if(this.classes.size() == 0) {
            HintManager.getInstance().showErrorHint(editor, "Ops, nothing found");
            return;
        }

        JBList<String> list = new JBList<>(this.classes);

        // single item, directly run it
        if(this.classes.size() == 1) {
            invoke(startElement, classes.iterator().next());
            return;
        }

        // suggestion possible import
        JBPopupFactory.getInstance().createListPopupBuilder(list)
            .setTitle("Import: Annotation Suggestion")
            .setItemChoosenCallback(() -> {
                String selectedValue = list.getSelectedValue();

                // sub thread run our own action
                new WriteCommandAction.Simple(editor.getProject(), "Import: " + selectedValue) {
                    @Override
                    protected void run() {
                        invoke(startElement, selectedValue);
                    }
                }.execute();
            })
            .createPopup()
            .showInBestPositionFor(editor);
    }

    private void invoke(@NotNull PsiElement psiElement, @NotNull String className) {
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(psiElement);
        if(scopeForUseOperator != null) {
            PhpAliasImporter.insertUseStatement(className, scopeForUseOperator);
        }
    }
}
