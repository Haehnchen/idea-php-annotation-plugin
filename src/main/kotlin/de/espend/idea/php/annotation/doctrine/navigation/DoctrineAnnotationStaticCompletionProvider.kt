package de.espend.idea.php.annotation.doctrine.navigation

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.jetbrains.php.lang.PhpLangUtil
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrineAnnotationStaticCompletionProvider : PhpAnnotationCompletionProvider {
    override fun getPropertyValueCompletions(
        annotationPropertyParameter: AnnotationPropertyParameter,
        completionParameter: AnnotationCompletionProviderParameter
    ) {
        if (annotationPropertyParameter.type != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return
        }

        if (annotationPropertyParameter.propertyName == "onDelete" && PhpLangUtil.equalsClassNames(
                annotationPropertyParameter.phpClass.presentableFQN,
                "Doctrine\\ORM\\Mapping\\JoinColumn",
            )
        ) {
            for (value in listOf("CASCADE", "SET NULL")) {
                completionParameter.result.addElement(LookupElementBuilder.create(value))
            }
        }
    }
}
