package de.espend.idea.php.annotation.doctrine.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineClassOrmAnnotationGenerateAction extends CodeInsightAction {

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {

        if(!(file instanceof PhpFile) || !DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        int offset = editor.getCaretModel().getOffset();
        if(offset <= 0) {
            return false;
        }

        PsiElement psiElement = file.findElementAt(offset);
        if(psiElement == null) {
            return false;
        }

        if(!PlatformPatterns.psiElement().inside(PhpClass.class).accepts(psiElement)) {
            return false;
        }

        PhpClass phpClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass.class);
        if(phpClass == null) {
            return false;
        }

        PhpDocComment docComment = phpClass.getDocComment();
        if(docComment == null) {
            return true;
        }

        PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment);
        if(container == null) {
            return true;
        }

        return container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Table") == null;
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
                PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(phpClass.getFirstChild());
                if(scopeForUseOperator != null) {
                    PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, DoctrineUtil.DOCTRINE_ORM_MAPPING, "ORM");
                    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
                }

                PhpDocUtil.addClassOrmDocs(phpClass, editor.getDocument(), file);
            }

            @Override
            public boolean startInWriteAction() {
                return true;
            }
        };
    }

}
