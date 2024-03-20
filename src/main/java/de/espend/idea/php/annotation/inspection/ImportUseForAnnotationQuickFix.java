package de.espend.idea.php.annotation.inspection;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ImportUseForAnnotationQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
    @NotNull
    private final Collection<Pair<String, String>>  classes;

    ImportUseForAnnotationQuickFix(@NotNull PhpDocTag phpDocTag, @NotNull Collection<Pair<String, String>>  classes) {
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
            Pair<String, String> next = this.classes.iterator().next();
            String message = "Import: " + StringUtils.stripStart(next.getFirst(), "\\");

            String alias = next.getSecond();
            if (alias != null) {
                message += " -> " + alias;
            }

            return message;
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

        // single item, directly run it
        if(this.classes.size() == 1) {
            invoke(startElement, classes.iterator().next());
            return;
        }

        // strip first "\"
        List<PopupChooserItem> popupChooserItems = this.classes.stream()
            .map(pair -> {
                PhpClass classInterface = PhpElementsUtil.getClassInterface(project, pair.getFirst());
                return new PopupChooserItem(pair, classInterface != null && classInterface.isDeprecated());
            })
            .collect(Collectors.toList());

        JBPopupFactory.getInstance().createPopupChooserBuilder(popupChooserItems)
            .setTitle("Import: Annotation Suggestion")
            .setItemChosenCallback(selected ->
                WriteCommandAction.writeCommandAction(editor.getProject())
                    .withName("Import: " + selected)
                    .run(() -> invoke(startElement, selected.item()))
            )
            .createPopup()
            .showInBestPositionFor(editor);
    }

    private void invoke(@NotNull PsiElement psiElement, @NotNull Pair<String, String> pair) {
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(psiElement);
        if(scopeForUseOperator != null) {
            if (pair.getSecond() == null) {
                PhpAliasImporter.insertUseStatement(pair.getFirst(), scopeForUseOperator);
            } else {
                PhpAliasImporter.insertUseStatement(pair.getFirst(), pair.getSecond(), scopeForUseOperator);
            }
        }
    }

    private record PopupChooserItem(@NotNull Pair<String, String> item, boolean isDeprecated) {
        @Override
        public String toString() {
            String itemText = StringUtils.stripStart(item.getFirst(), "\\");

            String alias = item.getSecond();
            if (alias != null) {
                itemText += " -> " + alias;
            }

            if (isDeprecated) {
                itemText += " (Deprecated)";
            }

            return itemText;
        }
    }
}
