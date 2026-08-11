package de.espend.idea.php.annotation.reference

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter
import de.espend.idea.php.annotation.pattern.AnnotationPattern
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpElementsUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationPropertyValueReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            AnnotationPattern.getDefaultPropertyValueString(),
            PropertyValueDefaultReferences(),
        )
        registrar.registerReferenceProvider(
            AnnotationPattern.getPropertyValueString(),
            PropertyValueReferences(),
        )

        // #[Route('/path', name: '<caret>')]
        registrar.registerReferenceProvider(
            AnnotationPattern.getAttributesValueReferencesPattern(),
            AttributeValueReferences(),
        )

        // #[Route('<caret>')]
        registrar.registerReferenceProvider(
            AnnotationPattern.getAttributesDefaultPattern(),
            AttributeDefaultReferences(),
        )
    }

    /**
     * '@Template("foo.twig.html")'
     * '@Service("foo")'
     */
    private inner class PropertyValueDefaultReferences : PsiReferenceProvider() {
        override fun getReferencesByElement(
            element: PsiElement,
            context: ProcessingContext,
        ): Array<PsiReference> {
            val phpClass = getValidAnnotationClass(element) ?: return emptyArray()
            val property = AnnotationPropertyParameter(element, phpClass, AnnotationPropertyParameter.Type.DEFAULT)
            return addPsiReferences(element, context, property)
        }
    }

    /**
     * '@Template(name="foo.twig.html")'
     */
    private inner class PropertyValueReferences : PsiReferenceProvider() {
        override fun getReferencesByElement(
            element: PsiElement,
            context: ProcessingContext,
        ): Array<PsiReference> {
            val phpClass = getValidAnnotationClass(element) ?: return emptyArray()
            val propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(element, DOC_IDENTIFIER_PATTERN)
                ?: return emptyArray()
            val property = AnnotationPropertyParameter(
                element,
                phpClass,
                propertyName.text,
                AnnotationPropertyParameter.Type.PROPERTY_VALUE,
            )
            return addPsiReferences(element, context, property)
        }
    }

    /**
     * `#[Route('/path', name: '<caret>')]`
     */
    private inner class AttributeValueReferences : PsiReferenceProvider() {
        override fun getReferencesByElement(
            element: PsiElement,
            context: ProcessingContext,
        ): Array<PsiReference> {
            val phpClass = getAttributeClass(element) ?: return emptyArray()
            val attributeNamePsi = PhpPsiUtil.getPrevSibling(
                element,
                { sibling ->
                    sibling is PsiWhiteSpace || sibling.node.elementType === PhpTokenTypes.opCOLON
                },
            )
            if (attributeNamePsi == null || attributeNamePsi.node.elementType !== PhpTokenTypes.IDENTIFIER) {
                return emptyArray()
            }

            val attributeName = attributeNamePsi.text
            if (StringUtils.isBlank(attributeName)) {
                return emptyArray()
            }

            val property = AnnotationPropertyParameter(
                element,
                phpClass,
                attributeName,
                AnnotationPropertyParameter.Type.PROPERTY_VALUE,
            )
            return addPsiReferences(element, context, property)
        }
    }

    /**
     * `#[Route('<caret>')]`
     */
    private inner class AttributeDefaultReferences : PsiReferenceProvider() {
        override fun getReferencesByElement(
            element: PsiElement,
            context: ProcessingContext,
        ): Array<PsiReference> {
            val phpClass = getAttributeClass(element) ?: return emptyArray()
            val property = AnnotationPropertyParameter(element, phpClass, AnnotationPropertyParameter.Type.DEFAULT)
            return addPsiReferences(element, context, property)
        }
    }

    private fun getValidAnnotationClass(element: PsiElement): PhpClass? {
        val phpDocTag = PsiTreeUtil.getParentOfType(element, PhpDocTag::class.java) ?: return null
        return AnnotationUtil.getAnnotationReference(phpDocTag)
    }

    private fun addPsiReferences(
        element: PsiElement,
        context: ProcessingContext,
        property: AnnotationPropertyParameter,
    ): Array<PsiReference> {
        val references = ArrayList<PsiReference>()
        val parameter = PhpAnnotationReferenceProviderParameter(element, context)

        for (extension in AnnotationUtil.EXTENSION_POINT_REFERENCES.extensions) {
            val extensionReferences = extension.getPropertyReferences(property, parameter)
            if (!extensionReferences.isNullOrEmpty()) {
                references.addAll(extensionReferences.filterNotNull())
            }
        }

        return references.toTypedArray()
    }

}

private val DOC_IDENTIFIER_PATTERN: ElementPattern<PsiElement> =
    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)

private fun getAttributeClass(element: PsiElement): PhpClass? {
    if (element !is StringLiteralExpression) {
        return null
    }

    val phpAttribute = PsiTreeUtil.getParentOfType(element, PhpAttribute::class.java) ?: return null
    val fqn = phpAttribute.fqn ?: return null
    return PhpElementsUtil.getClassInterface(element.project, fqn)
}
