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
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineAddRepositoryGenerateAction : CodeInsightAction() {
    override fun getHandler(): CodeInsightActionHandler {
        return object : CodeInsightActionHandler {
            override fun invoke(project: Project, editor: Editor, file: PsiFile) {
                val phpClass = getPhpClassOnValidScope(editor, file) ?: return
                DoctrineOrmRepositoryIntention().invoke(project, editor, phpClass.firstChild)
            }

            override fun startInWriteAction(): Boolean = true
        }
    }

    override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        val phpClass = getPhpClassOnValidScope(editor, file) ?: return false
        return DoctrineOrmRepositoryIntention().isAvailable(project, editor, phpClass.firstChild)
    }

    private companion object {
        val INSIDE_PHP_CLASS_PATTERN: ElementPattern<PsiElement> =
            PlatformPatterns.psiElement().inside(PhpClass::class.java)
        val INSIDE_PHP_DOC_COMMENT_PATTERN: ElementPattern<PsiElement> =
            PlatformPatterns.psiElement().inside(PhpDocComment::class.java)

        fun getPhpClassOnValidScope(editor: Editor, file: PsiFile): PhpClass? {
            val offset = editor.caretModel.offset
            if (offset <= 0) {
                return null
            }

            val psiElement = file.findElementAt(offset) ?: return null

            // attribute and direct hit
            if (INSIDE_PHP_CLASS_PATTERN.accepts(psiElement)) {
                return PsiTreeUtil.getParentOfType(psiElement, PhpClass::class.java)
            }

            // docblocks are outside the PhpClass scope
            if (INSIDE_PHP_DOC_COMMENT_PATTERN.accepts(psiElement)) {
                val docComment = PsiTreeUtil.getParentOfType(psiElement, PhpDocComment::class.java)
                val nextSibling = docComment?.nextPsiSibling
                if (nextSibling is PhpClass) {
                    return nextSibling
                }
            }

            return PsiTreeUtil.getParentOfType(psiElement, PhpClass::class.java)
        }
    }
}
