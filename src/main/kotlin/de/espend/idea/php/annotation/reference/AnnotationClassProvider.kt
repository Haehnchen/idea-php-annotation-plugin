package de.espend.idea.php.annotation.reference

import com.intellij.psi.PsiReference
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter
import de.espend.idea.php.annotation.reference.references.PhpClassReference

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationClassProvider : PhpAnnotationReferenceProvider {
    override fun getPropertyReferences(
        annotationPropertyParameter: AnnotationPropertyParameter,
        referencesByElementParameter: PhpAnnotationReferenceProviderParameter?
    ): Array<PsiReference>? {
        if (annotationPropertyParameter.type != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null
        }

        if (annotationPropertyParameter.propertyName != "targetEntity") {
            return null
        }

        return arrayOf(
            PhpClassReference(annotationPropertyParameter.element as StringLiteralExpression)
        )
    }
}
