package de.espend.idea.php.annotation.inspection.quickFix;

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.lang.psi.visitors.PhpRecursiveElementVisitor;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

abstract public class ReplaceAnnotationWithAnotherQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
    protected SmartPsiElementPointer<PhpClass> deprecatedClass;
    protected SmartPsiElementPointer<PhpClass> replacementClass;
    protected String preparedReplacement = null;

    public ReplaceAnnotationWithAnotherQuickFix(PhpDocTag phpDocTag, PhpClass deprecatedClass, PhpClass replacementClass) {
        super(phpDocTag);

        this.deprecatedClass = SmartPointerManager.createPointer(deprecatedClass);
        this.replacementClass = SmartPointerManager.createPointer(replacementClass);
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable("is null when called from inspection") Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        if (editor == null) {
            return;
        }

        preReplace(editor);

        invoke(startElement, deprecatedClass.getElement(), replacementClass.getElement());

        postReplace(editor);
    }

    protected void preReplace(Editor editor) {
    }

    protected void postReplace(Editor editor) {
    }

    protected void invoke(@NotNull PsiElement psiElement, @NotNull PhpClass deprecatedImport, @NotNull PhpClass replacementImport) {
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(psiElement);

        PhpUseListVisitor visitor = new PhpUseListVisitor();
        psiElement.getContainingFile().accept(visitor);

        for (PhpUseList useList : visitor.useLists) {
            for (PhpUse declaration : useList.getDeclarations()) {
                if (declaration.getFQN().equals(deprecatedImport.getFQN())) {
                    useList.delete();
                }
            }
        }

        if (scopeForUseOperator != null) {
            PhpAliasImporter.insertUseStatement(replacementImport.getFQN(), scopeForUseOperator);
        }
    }

    @NotNull
    @Override
    public String getText() {
        return "Replace: " + deprecatedClass.getElement().getFQN() + " with " + replacementClass.getElement().getFQN();
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Annotation";
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
