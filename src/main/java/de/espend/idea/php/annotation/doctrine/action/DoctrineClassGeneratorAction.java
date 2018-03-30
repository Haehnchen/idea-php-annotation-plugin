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
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
abstract public class DoctrineClassGeneratorAction extends CodeInsightAction {
    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!(file instanceof PhpFile) || !DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        int offset = editor.getCaretModel().getOffset();
        if (offset <= 0) {
            return false;
        }

        PsiElement psiElement = file.findElementAt(offset);
        if (psiElement == null) {
            return false;
        }

        if (!PlatformPatterns.psiElement().inside(PhpClass.class).accepts(psiElement)) {
            return false;
        }

        PhpClass phpClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
        if (phpClass == null) {
            return false;
        }

        PhpDocComment docComment = phpClass.getDocComment();
        if (docComment == null) {
            return true;
        }

        PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment);

        return container == null || container.getPhpDocBlock(supportedClass()) == null;
    }

    @NotNull
    @Override
    protected CodeInsightActionHandler getHandler() {
        return new CodeInsightActionHandler() {
            @Override
            public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {

                int offset = editor.getCaretModel().getOffset();
                if(offset <= 0) {
                    return;
                }

                PsiElement psiElement = file.findElementAt(offset);
                if(psiElement == null) {
                    return;
                }

                if(!PlatformPatterns.psiElement().inside(PhpClass.class).accepts(psiElement)) {
                    return;
                }

                PhpClass phpClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
                if(phpClass == null) {
                    return;
                }

                // insert ORM alias
                execute(editor, phpClass, file);
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }

    abstract protected void execute(@NotNull Editor editor, @NotNull PhpClass phpClass, @NotNull PsiFile psiFile);

    /**
     * Class supported by this action. if already inside DocBlock dont provide an action
     */
    @NotNull
    abstract protected String supportedClass();
}
