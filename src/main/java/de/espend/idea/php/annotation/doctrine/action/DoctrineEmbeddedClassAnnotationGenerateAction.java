package de.espend.idea.php.annotation.doctrine.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineEmbeddedClassAnnotationGenerateAction extends DoctrineClassGeneratorAction {
    @NotNull
    @Override
    protected String supportedClass() {
        return "Doctrine\\ORM\\Mapping\\Embedded";
    }

    protected void execute(@NotNull Editor editor, @NotNull PhpClass phpClass, @NotNull PsiFile psiFile) {
        // insert ORM alias
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(phpClass.getFirstChild());
        if(scopeForUseOperator != null) {
            PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, DoctrineUtil.DOCTRINE_ORM_MAPPING, "ORM");
            PsiDocumentManager.getInstance(psiFile.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
        }

        PhpDocUtil.addClassEmbeddedDocs(phpClass, editor.getDocument(), psiFile);
    }
}
