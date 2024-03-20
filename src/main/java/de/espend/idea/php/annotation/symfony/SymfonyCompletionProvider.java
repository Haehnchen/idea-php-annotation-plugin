package de.espend.idea.php.annotation.symfony;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.jetbrains.php.lang.PhpLangUtil;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SymfonyCompletionProvider implements PhpAnnotationCompletionProvider {
    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter parameter, AnnotationCompletionProviderParameter completion) {
        if(parameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_ARRAY) {
            return;
        }

        if("methods".equals(parameter.getPropertyName()) && PhpLangUtil.equalsClassNames(StringUtils.stripStart(parameter.getPhpClass().getFQN(), "\\"), "Symfony\\Component\\Routing\\Annotation\\Route")) {
            for (String s : new String[]{"HEAD", "GET", "POST", "PUT", "PATCH", "DELETE", "PURGE", "OPTIONS", "TRACE", "CONNECT"}) {
                completion.getResult().addElement(LookupElementBuilder.create(s));
            }
        }
    }
}
