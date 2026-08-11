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
import com.jetbrains.php.lang.actions.generation.PhpGenerateFieldAccessorHandlerBase
import com.jetbrains.php.lang.intentions.generators.PhpAccessorMethodData
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.util.PhpDocUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrinePropertyOrmAnnotationGenerateAction : CodeInsightAction() {
    private val myHandler = object : PhpGenerateFieldAccessorHandlerBase() {
        private lateinit var editor: Editor
        private lateinit var file: PsiFile

        override fun invoke(project: Project, editor: Editor, file: PsiFile) {
            this.editor = editor
            this.file = file
            super.invoke(project, editor, file)
        }

        override fun createAccessors(phpClass: PhpClass, field: PsiElement): Array<PhpAccessorMethodData> {
            if (field is Field) {
                DoctrineUtil.importOrmUseAliasIfNotExists(field)
                PhpDocUtil.addPropertyOrmDocs(field, editor.document, file)
            }

            return emptyArray()
        }

        override fun isSelectable(phpClass: PhpClass, field: Field): Boolean {
            return !DoctrineUtil.isOrmColumnProperty(field)
        }

        override fun getErrorMessage(): String = "No possible orm property found"

        override fun containsSetters(): Boolean = false
    }

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
        return phpClass.ownFields.isNotEmpty()
    }

    override fun getHandler(): CodeInsightActionHandler = myHandler

    private companion object {
        val INSIDE_PHP_CLASS_PATTERN: ElementPattern<PsiElement> =
            PlatformPatterns.psiElement().inside(PhpClass::class.java)
    }
}
