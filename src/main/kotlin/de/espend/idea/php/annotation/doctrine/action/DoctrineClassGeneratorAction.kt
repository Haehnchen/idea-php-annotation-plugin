package de.espend.idea.php.annotation.doctrine.action

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.util.AnnotationUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
abstract class DoctrineClassGeneratorAction : CodeInsightAction() {
    override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        if (file !is PhpFile || !DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false
        }

        val offset = editor.caretModel.offset
        if (offset <= 0) {
            return false
        }

        val psiElement = file.findElementAt(offset) ?: return false
        if (!INSIDE_PHP_CLASS_PATTERN.accepts(psiElement)) {
            return false
        }

        val phpClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass::class.java) ?: return false
        val docComment = phpClass.docComment
        if (docComment != null) {
            val container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment)
            if (container?.getPhpDocBlock(supportedClass()) != null) {
                return false
            }
        }

        val supportedClass = "\\" + StringUtils.stripStart(supportedClass(), "\\")
        return phpClass.getAttributes(supportedClass).isEmpty()
    }

    override fun getHandler(): CodeInsightActionHandler {
        return object : CodeInsightActionHandler {
            override fun invoke(project: Project, editor: Editor, file: PsiFile) {
                val offset = editor.caretModel.offset
                if (offset <= 0) {
                    return
                }

                val psiElement = file.findElementAt(offset) ?: return
                if (!INSIDE_PHP_CLASS_PATTERN.accepts(psiElement)) {
                    return
                }

                val phpClass = PsiTreeUtil.getParentOfType(psiElement, PhpClass::class.java) ?: return
                execute(editor, phpClass, file)
            }

            override fun startInWriteAction(): Boolean = true
        }
    }

    protected abstract fun execute(editor: Editor, phpClass: PhpClass, psiFile: PsiFile)

    /**
     * Class supported by this action. If it is already inside the DocBlock, do not provide an action.
     */
    protected abstract fun supportedClass(): String

    private companion object {
        val INSIDE_PHP_CLASS_PATTERN: ElementPattern<PsiElement> =
            PlatformPatterns.psiElement().inside(PhpClass::class.java)
    }
}
