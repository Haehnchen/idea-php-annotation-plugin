package de.espend.idea.php.annotation.doctrine.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Field;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * Intention: "private $id<carpet>;", @TODO: "private $id;<carpet>"
 */
public class DoctrineOrmFieldIntention extends PsiElementBaseIntentionAction {

    private Editor editor;

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {

        if(!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        PsiElement parent = element.getParent();
        if(parent == null || parent.getNode().getElementType() != PhpElementTypes.CLASS_FIELDS) {
            return false;
        }

        Field field = PsiTreeUtil.getChildOfType(parent, Field.class);
        return field != null && !DoctrineUtil.isOrmColumnProperty(field);

    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {

        PsiElement parent = psiElement.getParent();
        if(parent != null && parent.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS) {
            Field field = PsiTreeUtil.getChildOfType(parent, Field.class);
            if(field != null) {
                PhpDocUtil.addPropertyOrmDocs(field, editor.getDocument(), psiElement.getContainingFile());
            }
        }

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
