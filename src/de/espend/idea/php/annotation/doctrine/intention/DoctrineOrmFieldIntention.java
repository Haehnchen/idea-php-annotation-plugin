package de.espend.idea.php.annotation.doctrine.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.intentions.PhpAddFieldAccessorBase;
import com.jetbrains.php.lang.psi.elements.Field;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineOrmFieldIntention extends PhpAddFieldAccessorBase {

    private Editor editor;

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {

        if(!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        return super.isAvailable(project, editor, element);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        this.editor = editor;
        super.invoke(project, editor, psiElement);
    }

    @Override
    protected PhpCodeUtil.AccessorMethodData[] createAccessors(PsiElement psiElement) {

        if(psiElement instanceof Field) {
            PhpDocUtil.addPropertyOrmDocs((Field) psiElement, editor.getDocument(), psiElement.getContainingFile());
        }

        return new PhpCodeUtil.AccessorMethodData[0];
    }

    @Override
    protected boolean hasAccessors(Field field) {
        return DoctrineUtil.isOrmColumnProperty(field);
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "PhpAnnotations";
    }

    @NotNull
    @Override
    public String getText() {
        return "Add Doctrine Column";
    }

}
