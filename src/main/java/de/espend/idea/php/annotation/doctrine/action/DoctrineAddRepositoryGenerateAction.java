package de.espend.idea.php.annotation.doctrine.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineAddRepositoryGenerateAction extends CodeInsightAction {
    @NotNull
    @Override
    protected CodeInsightActionHandler getHandler() {
        return new CodeInsightActionHandler() {
            @Override
            public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
                PhpClass phpClass = getPhpClassOnValidScope(editor, file);
                if (phpClass == null) {
                    return;
                }

                new DoctrineOrmRepositoryIntention().invoke(project, editor, phpClass.getFirstChild());
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }

    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PhpClass phpClass = getPhpClassOnValidScope(editor, file);
        if (phpClass == null) {
            return false;
        }

        return new DoctrineOrmRepositoryIntention().isAvailable(project, editor, phpClass.getFirstChild());
    }


    @Nullable
    private static PhpClass getPhpClassOnValidScope(@NotNull Editor editor, @NotNull PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        if (offset <= 0) {
            return null;
        }

        PsiElement psiElement = file.findElementAt(offset);
        if (psiElement == null) {
            return null;
        }

        // attribute and direct hit
        if (PlatformPatterns.psiElement().inside(PhpClass.class).accepts(psiElement)) {
            return PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
        }

        // docblock are outside the phpclass scope
        if (PlatformPatterns.psiElement().inside(PhpDocComment.class).accepts(psiElement)) {
            PhpDocComment parentOfType = PsiTreeUtil.getParentOfType(psiElement, PhpDocComment.class);
            if (parentOfType != null) {
                PhpPsiElement nextPsiSibling = parentOfType.getNextPsiSibling();
                if (nextPsiSibling instanceof PhpClass) {
                    return (PhpClass) nextPsiSibling;
                }
            }
        }

        return PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
    }
}
