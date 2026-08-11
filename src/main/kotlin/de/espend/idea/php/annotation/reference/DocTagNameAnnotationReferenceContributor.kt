package de.espend.idea.php.annotation.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.ResolveResult
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocToken
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpNamespace
import com.jetbrains.php.lang.psi.elements.PhpUse
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpDocUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DocTagNameAnnotationReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PhpDocTag::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext,
                ): Array<PsiReference> {
                    if (element !is PhpDocTag) {
                        return emptyArray()
                    }
                    return arrayOf(PhpDocTagReference(element))
                }
            },
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PhpDocToken::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext,
                ): Array<PsiReference> {
                    if (element.node.elementType !== PhpDocTokenTypes.DOC_IDENTIFIER) {
                        return emptyArray()
                    }

                    val prevSibling = element.prevSibling
                    if (
                        prevSibling == null ||
                        prevSibling.node.elementType === PhpDocTokenTypes.DOC_NAMESPACE ||
                        PhpDocUtil.isDocStaticElement(prevSibling)
                    ) {
                        return emptyArray()
                    }

                    PhpDocUtil.getNamespaceForDocIdentifierAtStart(element) ?: return emptyArray()
                    val docTag: PhpDocTag? =
                        PhpPsiUtil.getParentByCondition(element, true, PhpDocTag.INSTANCEOF, null)
                    docTag ?: return emptyArray()
                    val fqn = AnnotationUtil.getClassFromDocIdentifierAsString(element) ?: return emptyArray()

                    return arrayOf(PhpDocIdentifierReference(element, fqn))
                }
            },
        )
    }

    private class PhpDocTagReference(element: PhpDocTag) : PsiPolyVariantReferenceBase<PhpDocTag>(element) {
        override fun isReferenceTo(element: PsiElement): Boolean {
            if (element is PhpUse) {
                if (element.name == getDocBlockName()) {
                    return true
                }
            }

            if (element is PhpNamespace) {
                val phpClass = AnnotationUtil.getAnnotationReference(this.element)
                return phpClass != null && phpClass.fqn.startsWith(element.fqn)
            }

            if (element is PhpClass) {
                val phpClass = AnnotationUtil.getAnnotationReference(this.element)
                return phpClass != null && phpClass.fqn == element.fqn
            }

            return false
        }

        override fun getRangeInElement(): TextRange {
            var tagName = element.name
            var rangeStart = 0
            var rangeEnd = tagName.length

            if (tagName.startsWith("@")) {
                rangeStart = 1
                tagName = tagName.substring(1)
            }

            if (tagName.contains("\\")) {
                rangeEnd = tagName.indexOf("\\") + rangeStart
            }

            return TextRange(rangeStart, rangeEnd)
        }

        override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
            val phpClass = AnnotationUtil.getAnnotationReference(element) ?: return emptyArray()
            return arrayOf(PsiElementResolveResult(phpClass))
        }

        override fun getVariants(): Array<Any> = emptyArray()

        private fun getDocBlockName(): String {
            var name = element.name
            if (name.startsWith("@")) {
                name = name.substring(1)
            }
            if (name.contains("\\")) {
                name = name.substring(0, name.indexOf("\\"))
            }
            return name
        }
    }

    private class PhpDocIdentifierReference(
        element: PsiElement,
        private val fqn: String,
    ) : PsiReferenceBase<PsiElement>(element) {
        override fun getRangeInElement(): TextRange = TextRange.create(0, element.textLength)

        override fun resolve(): PsiElement? = null

        override fun isReferenceTo(psiElement: PsiElement): Boolean {
            if (psiElement !is PhpNamedElement) {
                return false
            }

            val text = element.text
            if (StringUtils.isBlank(text)) {
                return false
            }

            val classByContext = AnnotationUtil.getUseImportMap(element)[text]
            if (classByContext != null) {
                return StringUtils.stripStart(psiElement.fqn, "\\").equals(
                    StringUtils.stripStart(fqn, "\\"),
                    ignoreCase = true,
                )
            }

            return false
        }

        override fun getVariants(): Array<Any> = emptyArray()
    }
}
