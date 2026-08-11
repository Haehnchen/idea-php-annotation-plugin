package de.espend.idea.php.annotation.doctrine.reference

import com.intellij.psi.PsiReference
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.espend.idea.php.annotation.doctrine.reference.references.DoctrineRepositoryReference
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineAnnotationTypeProvider : PhpAnnotationReferenceProvider {
    override fun getPropertyReferences(
        annotationPropertyParameter: AnnotationPropertyParameter,
        referencesByElementParameter: PhpAnnotationReferenceProviderParameter?
    ): Array<PsiReference>? {
        if (annotationPropertyParameter.type != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null
        }

        if (annotationPropertyParameter.propertyName != "repositoryClass") {
            return null
        }

        val presentableFQN = annotationPropertyParameter.phpClass.presentableFQN
        if (!PhpLangUtil.equalsClassNames("Doctrine\\ORM\\Mapping\\Entity", presentableFQN)) {
            return null
        }

        return arrayOf(
            DoctrineRepositoryReference(annotationPropertyParameter.element as StringLiteralExpression)
        )
    }
}
