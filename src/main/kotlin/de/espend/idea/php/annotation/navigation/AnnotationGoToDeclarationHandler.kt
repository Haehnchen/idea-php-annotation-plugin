package de.espend.idea.php.annotation.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.extension.parameter.AnnotationDocTagGotoHandlerParameter
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyTargetsParameter
import de.espend.idea.php.annotation.pattern.AnnotationPattern
import de.espend.idea.php.annotation.util.AnnotationUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationGoToDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor,
    ): Array<PsiElement> {
        val psiElement = sourceElement ?: return emptyArray()
        val targets = ArrayList<PsiElement>()

        // @Test(<foo>=)
        if (AnnotationPattern.getDocAttribute().accepts(psiElement)) {
            addPropertyGoto(psiElement, targets)
        }

        // <@Test>, <@Test\Test>
        if (DOC_TAG_NAME_PATTERN.accepts(psiElement)) {
            addDocTagNameGoto(psiElement, targets)
        }

        // @Route(name=<ClassName>::FOO)
        if (DOC_IDENTIFIER_BEFORE_STATIC_PATTERN.accepts(psiElement)) {
            addStaticClassTargets(psiElement, targets)
        }

        // @Route(name=ClassName::<FOO>)
        if (AnnotationPattern.getClassConstant().accepts(psiElement)) {
            addStaticClassConstTargets(psiElement, targets)
        }

        return targets.toTypedArray()
    }

    private fun addDocTagNameGoto(psiElement: PsiElement, targets: MutableList<PsiElement>) {
        val phpDocTag = psiElement.context
        if (phpDocTag !is PhpDocTag) {
            return
        }

        val phpClass = AnnotationUtil.getAnnotationReference(phpDocTag) ?: return
        targets.add(phpClass)

        val parameter = AnnotationDocTagGotoHandlerParameter(phpDocTag, phpClass, targets)
        for (extension in AnnotationUtil.EP_DOC_TAG_GOTO.extensions) {
            extension.getGotoDeclarationTargets(parameter)
        }
    }

    private fun addPropertyGoto(psiElement: PsiElement, targets: MutableList<PsiElement>) {
        val phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag::class.java) ?: return
        val phpClass = AnnotationUtil.getAnnotationReference(phpDocTag) ?: return
        val property = psiElement.text
        if (StringUtils.isBlank(property)) {
            return
        }

        AnnotationUtil.visitAttributes(phpClass) { attributeName, _, target ->
            if (attributeName == property) {
                targets.add(target)
            }
            null
        }

        var parameter: AnnotationVirtualPropertyTargetsParameter? = null
        for (extension in AnnotationUtil.EP_VIRTUAL_PROPERTIES.extensions) {
            if (parameter == null) {
                parameter = AnnotationVirtualPropertyTargetsParameter(phpClass, psiElement, property)
            }
            extension.getTargets(parameter)
        }

        parameter?.let { targets.addAll(it.targets) }
    }

    private fun addStaticClassTargets(psiElement: PsiElement, targets: MutableList<PsiElement>) {
        AnnotationUtil.getClassFromDocIdentifier(psiElement)?.let { targets.add(it) }
    }

    private fun addStaticClassConstTargets(psiElement: PsiElement, targets: MutableList<PsiElement>) {
        val phpClass = AnnotationUtil.getClassFromConstant(psiElement) ?: return
        val constName = psiElement.text

        if (constName == "class") {
            targets.add(phpClass)
            return
        }

        phpClass.findFieldByName(constName, true)?.let { targets.add(it) }
    }

    override fun getActionText(context: DataContext): String? = null

    private companion object {
        val DOC_TAG_NAME_PATTERN: ElementPattern<PsiElement> =
            PlatformPatterns.psiElement(PhpDocElementTypes.DOC_TAG_NAME)
                .withText(StandardPatterns.string().startsWith("@"))
                .withLanguage(PhpLanguage.INSTANCE)

        val DOC_IDENTIFIER_BEFORE_STATIC_PATTERN: ElementPattern<PsiElement> =
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
                .beforeLeaf(AnnotationPattern.getDocStaticPattern())
                .withLanguage(PhpLanguage.INSTANCE)
    }
}
