package de.espend.idea.php.annotation.doctrine.reference.references

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.jetbrains.php.PhpIcons
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.espend.idea.php.annotation.util.PhpElementsUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineRepositoryReference(
    psiElement: StringLiteralExpression,
) : PsiPolyVariantReferenceBase<PsiElement>(psiElement) {
    private val content = psiElement.contents

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val phpClass = PhpElementsUtil.getClassInsideAnnotation(element as StringLiteralExpression, content)
            ?: return emptyArray()

        return arrayOf(PsiElementResolveResult(phpClass))
    }

    override fun getVariants(): Array<Any> {
        val phpClasses = mutableListOf<PhpClass>()
        PhpIndex.getInstance(element.project)
            .processAllSubclasses("\\Doctrine\\Common\\Persistence\\ObjectRepository") { phpClass ->
                phpClasses.add(phpClass)
                true
            }

        return phpClasses.map { phpClass ->
            LookupElementBuilder.create(phpClass.presentableFQN).withIcon(PhpIcons.CLASS)
        }.toTypedArray()
    }
}
