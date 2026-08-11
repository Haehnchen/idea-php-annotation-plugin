package de.espend.idea.php.annotation.doctrine.reference

import com.jetbrains.php.lang.PhpLangUtil
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * "@ORM\CustomIdGenerator(class="FooBar\CompositeIdGenerator")"
 */
class DoctrineCustomIdGenerator : ClassCompletionProviderAbstract() {
    override fun supports(parameter: AnnotationPropertyParameter): Boolean {
        return parameter.type == AnnotationPropertyParameter.Type.PROPERTY_VALUE &&
                parameter.propertyName == "class" &&
                PhpLangUtil.equalsClassNames(
                    StringUtils.stripStart(parameter.phpClass.fqn, "\\"),
                    "Doctrine\\ORM\\Mapping\\CustomIdGenerator"
                )
    }
}
