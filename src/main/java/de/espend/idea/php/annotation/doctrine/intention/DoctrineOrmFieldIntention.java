package de.espend.idea.php.annotation.doctrine.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * Intention: "private $id<carpet>;"
 */
public class DoctrineOrmFieldIntention extends PsiElementBaseIntentionAction {
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if(!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        Field context = getFieldContext(element);
        return context != null && !DoctrineUtil.isOrmColumnProperty(context);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        Field context = getFieldContext(psiElement);
        if (context != null) {
            DoctrineUtil.importOrmUseAliasIfNotExists(context);
            PhpDocUtil.addPropertyOrmDocs(context, editor.getDocument(), psiElement.getContainingFile());
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
        return "Add Doctrine column";
    }

    private Field getFieldContext(@NotNull PsiElement element) {
        Field context = null;
        if (element instanceof Field) {
            context = (Field) element;
        } else {
            // direct field context
            // public $foo;
            PsiElement firstParent = PsiTreeUtil.findFirstParent(element, psiElement -> psiElement.getNode() != null && psiElement.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS);
            if (firstParent instanceof PhpPsiElement) {
                context = PsiTreeUtil.getChildOfType(firstParent, Field.class);
            }

            // docblock before field
            // /** <caret> /*
            // public $foo;
            if (context == null) {
                PhpDocComment parentOfType = PsiTreeUtil.getParentOfType(element, PhpDocComment.class);
                if (parentOfType != null) {
                    PhpPsiElement nextPsiSibling = parentOfType.getNextPsiSibling();
                    if (nextPsiSibling != null && nextPsiSibling.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS) {
                        context = PsiTreeUtil.getChildOfType(nextPsiSibling, Field.class);
                    }
                }
            }

            // at the end of the line
            // public $foo;<caret>
            if (element instanceof PsiWhiteSpace) {
                PsiElement prevSibling = element.getPrevSibling();
                if (prevSibling != null && prevSibling.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS) {
                    context = PsiTreeUtil.getChildOfType(prevSibling, Field.class);
                }
            }
        }

        return context;
    }
}
