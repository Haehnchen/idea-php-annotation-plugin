package de.espend.idea.php.annotation.doctrine.reference

import com.intellij.psi.*
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineAnnotationFieldTypeProvider : PhpAnnotationReferenceProvider {
    override fun getPropertyReferences(
        annotationPropertyParameter: AnnotationPropertyParameter,
        referencesByElementParameter: PhpAnnotationReferenceProviderParameter?
    ): Array<PsiReference>? {
        if (annotationPropertyParameter.type != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null
        }

        if (annotationPropertyParameter.propertyName != "type") {
            return null
        }

        var presentableFQN = annotationPropertyParameter.phpClass.presentableFQN
        if (!presentableFQN.startsWith("\\")) {
            presentableFQN = "\\" + presentableFQN
        }

        if (presentableFQN != "\\Doctrine\\ORM\\Mapping\\Column") {
            return null
        }

        return arrayOf(
            DoctrineColumnTypeReference(annotationPropertyParameter.element as StringLiteralExpression)
        )
    }

    private class DoctrineColumnTypeReference(psiElement: StringLiteralExpression) :
        PsiPolyVariantReferenceBase<PsiElement>(psiElement) {
        override fun multiResolve(b: Boolean): Array<ResolveResult> {
            val stringLiteral = super.getElement() as StringLiteralExpression
            val contents = stringLiteral.contents
            if (StringUtils.isBlank(contents)) {
                return emptyArray()
            }

            return PsiElementResolveResult.createResults(
                DoctrineUtil.getColumnTypesTargets(stringLiteral.project, contents)
            )
        }

        override fun getVariants(): Array<Any> {
            val stringLiteral = super.getElement() as StringLiteralExpression
            return DoctrineUtil.getTypes(stringLiteral.project).toTypedArray()
        }
    }
}
