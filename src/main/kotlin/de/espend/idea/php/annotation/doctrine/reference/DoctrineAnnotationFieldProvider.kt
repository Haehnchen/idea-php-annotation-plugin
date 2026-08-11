package de.espend.idea.php.annotation.doctrine.reference

import com.intellij.psi.PsiReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.espend.idea.php.annotation.doctrine.reference.references.DoctrinePhpClassFieldReference
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter
import de.espend.idea.php.annotation.pattern.AnnotationPattern
import de.espend.idea.php.annotation.util.PhpElementsUtil
import de.espend.idea.php.annotation.util.PhpPsiAttributesUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineAnnotationFieldProvider : PhpAnnotationReferenceProvider {
    override fun getPropertyReferences(
        annotationPropertyParameter: AnnotationPropertyParameter,
        referencesByElementParameter: PhpAnnotationReferenceProviderParameter?
    ): Array<PsiReference>? {
        if (annotationPropertyParameter.type != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null
        }

        val propertyName = annotationPropertyParameter.propertyName
        if (propertyName != "mappedBy" && propertyName != "inversedBy") {
            return null
        }

        val parent = annotationPropertyParameter.element.parent

        if (parent is ParameterList) {
            val phpAttribute = parent.parent
            if (phpAttribute is PhpAttribute) {
                val targetEntityValue = PhpPsiAttributesUtil.getAttributeValueByNameAsStringWithClassConstant(
                    phpAttribute, "targetEntity"
                )
                if (targetEntityValue != null) {
                    val phpClass = PhpElementsUtil.getClassInterface(
                        annotationPropertyParameter.project,
                        targetEntityValue,
                    ) ?: return null

                    return arrayOf(
                        DoctrinePhpClassFieldReference(
                            annotationPropertyParameter.element as StringLiteralExpression,
                            phpClass
                        )
                    )
                }
            }
        } else {
            val targetEntity = PhpElementsUtil.getChildrenOnPatternMatch<StringLiteralExpression?>(
                parent,
                AnnotationPattern.getPropertyIdentifierValue("targetEntity")
            ) ?: return null

            val phpClass = PhpElementsUtil.getClassInsideAnnotation(targetEntity) ?: return null

            return arrayOf(
                DoctrinePhpClassFieldReference(
                    annotationPropertyParameter.element as StringLiteralExpression,
                    phpClass
                )
            )
        }

        return null
    }
}
