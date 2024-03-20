package de.espend.idea.php.annotation.doctrine.reference;

import com.jetbrains.php.lang.PhpLangUtil;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * "@ORM\CustomIdGenerator(class="FooBar\CompositeIdGenerator")"
 */
public class DoctrineCustomIdGenerator extends ClassCompletionProviderAbstract {
    @Override
    boolean supports(AnnotationPropertyParameter parameter) {
        return
            parameter.getType() == AnnotationPropertyParameter.Type.PROPERTY_VALUE &&
            "class".equals(parameter.getPropertyName()) &&
            PhpLangUtil.equalsClassNames(StringUtils.stripStart(parameter.getPhpClass().getFQN(), "\\"), "Doctrine\\ORM\\Mapping\\CustomIdGenerator");
    }
}
