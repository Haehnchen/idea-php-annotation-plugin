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
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.lang.psi.visitors.PhpRecursiveElementVisitor;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReplaceImportForDeprecatedAnnotationQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
    @NotNull
    private final Collection<String> classes;
    @NotNull
    private final String fqnToReplace;

    ReplaceImportForDeprecatedAnnotationQuickFix(@NotNull PhpDocTag phpDocTag, @NotNull String fqnToReplace, @NotNull Collection<String> classes) {
        super(phpDocTag);

        this.classes = classes;
        this.fqnToReplace = fqnToReplace;
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
            return "Replace: " + fqnToReplace + " with " + this.classes.iterator().next();
        }

        // multiple classes found just display overall count
        return String.format("Replace %s (%s)", this.fqnToReplace, this.classes.size());
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
            invoke(startElement, fqnToReplace,classes.iterator().next());
            return;
        }

        // suggestion possible import
        JBPopupFactory.getInstance().createListPopupBuilder(list)
            .setTitle("Import: Annotation replacement Suggestion")
            .setItemChoosenCallback(() -> {
                String selectedValue = list.getSelectedValue();

                // sub thread run our own action
                WriteCommandAction.runWriteCommandAction(editor.getProject(), "Import: " + selectedValue, "PHP Annotations", () -> {
                    invoke(startElement, fqnToReplace, selectedValue);
                });
            })
            .createPopup()
            .showInBestPositionFor(editor);
    }

    private void invoke(@NotNull PsiElement psiElement, @NotNull String deprecatedImport, @NotNull String replacementImport) {
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(psiElement);

        PhpUseListVisitor visitor = new PhpUseListVisitor();

        psiElement.getContainingFile().accept(visitor);

        for (PhpUseList useList : visitor.useLists) {
            for (PhpUse declaration : useList.getDeclarations()) {
                if (declaration.getFQN().equals(deprecatedImport)) {
                    useList.delete();
                }
            }
        }

        if(scopeForUseOperator != null) {
            PhpAliasImporter.insertUseStatement(replacementImport, scopeForUseOperator);
        }
    }

    private static class PhpUseListVisitor extends PhpRecursiveElementVisitor {
        public List<PhpUseList> useLists = new ArrayList<>();

        @Override
        public void visitPhpUseList(PhpUseList useList) {
            useLists.add(useList);

            super.visitPhpUseList(useList);
        }
    }
}
