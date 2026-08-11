package de.espend.idea.php.annotation.doctrine.action

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.util.PhpDocUtil
import de.espend.idea.php.annotation.util.PhpElementsUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineEmbeddedClassAnnotationGenerateAction : DoctrineClassGeneratorAction() {
    override fun supportedClass(): String = "Doctrine\\ORM\\Mapping\\Embeddable"

    override fun execute(editor: Editor, phpClass: PhpClass, psiFile: PsiFile) {
        val scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(phpClass.firstChild)
        if (scopeForUseOperator != null) {
            PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, DoctrineUtil.DOCTRINE_ORM_MAPPING, "ORM")
            PsiDocumentManager.getInstance(psiFile.project)
                .doPostponedOperationsAndUnblockDocument(editor.document)
        }

        PhpDocUtil.addClassEmbeddedDocs(phpClass, editor.document, psiFile)
    }
}
