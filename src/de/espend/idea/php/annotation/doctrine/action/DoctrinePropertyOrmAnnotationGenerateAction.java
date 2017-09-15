package de.espend.idea.php.annotation.doctrine.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.actions.generation.PhpGenerateFieldAccessorHandlerBase;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorMethodData;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrinePropertyOrmAnnotationGenerateAction extends CodeInsightAction {

    private final PhpGenerateFieldAccessorHandlerBase myHandler = new PhpGenerateFieldAccessorHandlerBase()
    {

        private Editor editor;
        private PsiFile file;

        @Override
        public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
            this.editor = editor;
            this.file = file;
            super.invoke(project, editor, file);
        }

        protected PhpAccessorMethodData[] createAccessors(PsiElement field) {
            // deprecated signature
            return createAccessors(null, field);
        }

        protected PhpAccessorMethodData[] createAccessors(PhpClass phpClass, PsiElement field)
        {
            if(field instanceof Field) {
                DoctrineUtil.importOrmUseAliasIfNotExists((Field) field);
                PhpDocUtil.addPropertyOrmDocs((Field) field, this.editor.getDocument(), file);
            }

            return new PhpAccessorMethodData[0];
        }

        protected boolean isSelectable(PhpClass phpClass, Field field)
        {
            return !DoctrineUtil.isOrmColumnProperty(field);
        }

        protected String getErrorMessage()
        {
            return "No possible orm property found";
        }

      @Override
      protected boolean containsSetters() {
        return false;
      }
    };

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

        return true;
    }

    @NotNull
    @Override
    protected CodeInsightActionHandler getHandler() {
        return myHandler;
    }
}
