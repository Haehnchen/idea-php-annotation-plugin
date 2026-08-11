package de.espend.idea.php.annotation.symfony

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.jetbrains.php.lang.PhpLangUtil
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class SymfonyCompletionProvider : PhpAnnotationCompletionProvider {
    override fun getPropertyValueCompletions(
        parameter: AnnotationPropertyParameter,
        completion: AnnotationCompletionProviderParameter
    ) {
        if (parameter.type != AnnotationPropertyParameter.Type.PROPERTY_ARRAY) {
            return
        }

        val supportsMethods = parameter.propertyName == "methods" &&
                (PhpLangUtil.equalsClassNames(
                    StringUtils.stripStart(parameter.phpClass.fqn, "\\"),
                    "Symfony\\Component\\Routing\\Annotation\\Route"
                ) || PhpLangUtil.equalsClassNames(
                    StringUtils.stripStart(parameter.phpClass.fqn, "\\"),
                    "Symfony\\Component\\Routing\\Attribute\\Route"
                )
                        )

        if (supportsMethods) {
            for (method in arrayOf(
                "HEAD",
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "PURGE",
                "OPTIONS",
                "TRACE",
                "CONNECT"
            )) {
                completion.result.addElement(LookupElementBuilder.create(method))
            }
        }
    }
}
