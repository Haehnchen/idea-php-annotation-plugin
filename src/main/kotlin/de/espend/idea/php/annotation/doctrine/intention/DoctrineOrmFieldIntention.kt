package de.espend.idea.php.annotation.doctrine.intention

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.parser.PhpElementTypes
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import de.espend.idea.php.annotation.PhpAnnotationIcons
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.util.PhpDocUtil
import javax.swing.Icon

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * Intention: "private $id<carpet>;"
 */
open class DoctrineOrmFieldIntention : PsiElementBaseIntentionAction(), Iconable, HighPriorityAction {
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false
        }

        val context = getFieldContext(element)
        return context != null && !DoctrineUtil.isOrmColumnProperty(context)
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor?, psiElement: PsiElement) {
        val document = editor?.document ?: return
        val context = getFieldContext(psiElement)
        if (context != null) {
            DoctrineUtil.importOrmUseAliasIfNotExists(context)
            PhpDocUtil.addPropertyOrmDocs(context, document, psiElement.containingFile)
        }
    }

    override fun getFamilyName(): String = "PhpAnnotations"

    override fun getText(): String = "Add Doctrine column"

    private fun getFieldContext(element: PsiElement): Field? {
        var context: Field? = null
        if (element is Field) {
            context = element
        } else {
            // direct field context
            // public $foo;
            val firstParent = PsiTreeUtil.findFirstParent(element) {
                it.node?.elementType === PhpElementTypes.CLASS_FIELDS
            }
            if (firstParent is PhpPsiElement) {
                context = PsiTreeUtil.getChildOfType(firstParent, Field::class.java)
            }

            // docblock before field
            // /** <caret> /*
            // public $foo;
            if (context == null) {
                val parentOfType = PsiTreeUtil.getParentOfType(element, PhpDocComment::class.java)
                if (parentOfType != null) {
                    val nextPsiSibling = parentOfType.nextPsiSibling
                    if (nextPsiSibling != null && nextPsiSibling.node.elementType === PhpElementTypes.CLASS_FIELDS) {
                        context = PsiTreeUtil.getChildOfType(nextPsiSibling, Field::class.java)
                    }
                }
            }

            // at the end of the line
            // public $foo;<caret>
            if (element is PsiWhiteSpace) {
                val prevSibling = element.prevSibling
                if (prevSibling != null && prevSibling.node.elementType === PhpElementTypes.CLASS_FIELDS) {
                    context = PsiTreeUtil.getChildOfType(prevSibling, Field::class.java)
                }
            }
        }

        return context
    }

    override fun getIcon(flags: Int): Icon = PhpAnnotationIcons.DOCTRINE
}
