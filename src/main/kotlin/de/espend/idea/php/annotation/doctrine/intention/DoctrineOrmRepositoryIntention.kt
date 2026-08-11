package de.espend.idea.php.annotation.doctrine.intention

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import de.espend.idea.php.annotation.PhpAnnotationIcons
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpDocUtil
import de.espend.idea.php.annotation.util.PhpElementsUtil
import de.espend.idea.php.annotation.util.PhpPsiAttributesUtil
import org.apache.commons.lang3.StringUtils
import org.jetbrains.annotations.Nls
import java.util.Properties
import javax.swing.Icon

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
open class DoctrineOrmRepositoryIntention :
    PsiElementBaseIntentionAction(),
    LocalQuickFix,
    Iconable,
    HighPriorityAction {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false
        }

        val phpClass = getScopedPhpClass(element) ?: return false
        return DoctrineUtil.hasCreateRepositoryClassSupport(phpClass)
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val phpClass = getScopedPhpClass(element) ?: return
        val currentEditor = editor ?: FileEditorManager.getInstance(project).selectedTextEditor ?: return

        // skip for preview
        val containingDirectory = phpClass.containingFile.containingDirectory ?: return
        val fqn = phpClass.fqn
        val sameNamespace = fqn + "Repository"
        if (PhpElementsUtil.getClass(project, sameNamespace) != null) {
            insertRepositoryClass(currentEditor, element, phpClass, sameNamespace)
            return
        }

        val split = StringUtils.split(StringUtils.stripStart(fqn, "\\"), "\\")

        // Foo\Entity\Foobar => Foo\Repository\FoobarRepository
        if (split.size > 2) {
            val repoNamespace = "\\" +
                split.copyOfRange(0, split.size - 2).joinToString("\\") +
                "\\Repository\\" + split[split.size - 1] + "Repository"
            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(currentEditor, element, phpClass, repoNamespace)
                return
            }
        }

        // Foo\Entity\Foo\Foobar => Foo\Repository\Foo\FoobarRepository
        val entityIndex = fqn.lastIndexOf("\\Entity\\")
        if (entityIndex > 0) {
            val repoNamespace = StringBuilder(fqn)
                .replace(entityIndex, entityIndex + "\\Entity\\".length, "\\Repository\\")
                .append("Repository")
                .toString()

            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(currentEditor, element, phpClass, repoNamespace)
                return
            }
        }

        // Foo\Entity\Foobar => Foo\Entity\Repository\FoobarRepository
        val namespaceIndex = fqn.lastIndexOf("\\")
        if (namespaceIndex > 0) {
            val repoNamespace = StringBuilder(fqn)
                .insert(namespaceIndex, "\\Repository")
                .toString() + "Repository"

            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(currentEditor, element, phpClass, repoNamespace)
                return
            }
        }

        val fileName = phpClass.name + "Repository.php"
        var repositoryDir: PsiDirectory? = null
        val parentDirectory = containingDirectory.parentDirectory
        if (parentDirectory != null) {
            repositoryDir = parentDirectory.findSubdirectory("Repository")
            if (repositoryDir == null) {
                repositoryDir = parentDirectory.createSubdirectory("Repository")
            }
        }

        if (repositoryDir == null) {
            if (!ApplicationManager.getApplication().isHeadlessEnvironment) {
                HintManager.getInstance()
                    .showErrorHint(currentEditor, "Repository directory structure can not be created")
            }

            return
        }

        if (repositoryDir.findFile(fileName) != null) {
            if (!ApplicationManager.getApplication().isHeadlessEnvironment) {
                HintManager.getInstance().showErrorHint(currentEditor, "Repository file already exists")
            }

            return
        }

        val templateName = if (
            PhpElementsUtil.getClass(
                project,
                "\\Doctrine\\Bundle\\DoctrineBundle\\Repository\\ServiceEntityRepository",
            ) != null
        ) {
            "Doctrine Entity ServiceRepository"
        } else {
            "Doctrine Entity Repository"
        }

        val fileTemplate = FileTemplateManager.getInstance(project).getInternalTemplate(templateName)
        val defaultProperties = FileTemplateManager.getInstance(project).defaultProperties
        val properties = Properties(defaultProperties)

        // Foo\Entity\Foobar => Foo\Repository\FoobarRepository
        val repoClass = "\\" +
            split.copyOfRange(0, split.size - 2).joinToString("\\") +
            "\\Repository\\" + split[split.size - 1] + "Repository"

        properties.setProperty(
            "NAMESPACE",
            StringUtils.stripStart(repoClass.take(repoClass.lastIndexOf("\\")), "\\"),
        )
        properties.setProperty("NAME", phpClass.name + "Repository")
        properties.setProperty("ENTITY_NAMESPACE", DoctrineUtil.trimBlackSlashes(phpClass.namespaceName))
        properties.setProperty("ENTITY_NAME", phpClass.name)

        val newElement = try {
            FileTemplateUtil.createFromTemplate(fileTemplate, fileName, properties, repositoryDir)
        } catch (_: Exception) {
            return
        }

        insertRepositoryClass(currentEditor, element, phpClass, repoClass)
        OpenFileDescriptor(project, newElement.containingFile.virtualFile, 0).navigate(true)
    }

    /**
     * Scope resolve for PhpClass:
     * "@ORM\Entity" or inside PhpClass
     */
    private fun getScopedPhpClass(element: PsiElement): PhpClass? {
        // inside "@ORM\Entity"
        var parent = element.parent

        // inside "@ORM\Entity(<caret>)"
        if (parent.node.elementType === PhpDocElementTypes.phpDocAttributeList) {
            parent = parent.parent
        }

        if (parent is PhpDocTag) {
            val phpDocAnnotationContainer = AnnotationUtil.getPhpDocAnnotationContainer(parent)
            if (phpDocAnnotationContainer != null) {
                val phpClass = phpDocAnnotationContainer.phpClass
                if (phpClass.presentableFQN == "Doctrine\\ORM\\Mapping\\Entity") {
                    val docTag = parent.parent
                    if (docTag is PhpDocComment) {
                        val nextPsiSibling = docTag.nextPsiSibling
                        if (nextPsiSibling is PhpClass) {
                            return nextPsiSibling
                        }
                    }
                }
            }

            return null
        }

        // and finally check PhpClass class scope
        return PsiTreeUtil.getParentOfType(element, PhpClass::class.java)
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    override fun getName(): String = text

    override fun getFamilyName(): String = "PhpAnnotations"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val psiElement = descriptor.psiElement ?: return
        val containingFile = psiElement.containingFile ?: return
        invoke(project, null, containingFile)
    }

    override fun getText(): String = "Add Doctrine repository"

    override fun getIcon(flags: Int): Icon = PhpAnnotationIcons.DOCTRINE

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo =
        IntentionPreviewInfo.EMPTY

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo =
        IntentionPreviewInfo.EMPTY
}

private fun insertRepositoryClass(
    editor: Editor,
    element: PsiElement,
    phpClass: PhpClass,
    repoClass: String,
) {
    val scopeForUseOperator: PhpPsiElement = PhpCodeInsightUtil.findScopeForUseOperator(element) ?: return

    PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, repoClass, null)
    PsiDocumentManager.getInstance(element.project)
        .doPostponedOperationsAndUnblockDocument(editor.document)

    val phpDocTagName = PhpDocUtil.getQualifiedName(phpClass, repoClass)
    val ormEntityPhpDocBlock: PhpDocTagAnnotation? = DoctrineUtil.getOrmEntityPhpDocBlock(phpClass)
    if (ormEntityPhpDocBlock != null) {
        AnnotationUtil.insertNamedArgumentForAnnotation(
            editor,
            ormEntityPhpDocBlock.phpDocTag,
            "repositoryClass",
            "$phpDocTagName::class",
        )

        return
    }

    val attributes: Collection<PhpAttribute> =
        phpClass.getAttributes("\\Doctrine\\ORM\\Mapping\\Entity")
    if (attributes.isNotEmpty()) {
        PhpPsiAttributesUtil.insertNamedArgumentForAttribute(
            editor,
            attributes.iterator().next(),
            "repositoryClass",
            "$phpDocTagName::class",
        )
    }
}
