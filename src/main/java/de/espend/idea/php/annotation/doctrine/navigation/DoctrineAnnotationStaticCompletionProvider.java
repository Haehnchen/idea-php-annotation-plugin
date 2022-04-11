package de.espend.idea.php.annotation.doctrine.navigation;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.jetbrains.php.lang.PhpLangUtil;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;

import java.util.Arrays;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineAnnotationStaticCompletionProvider implements PhpAnnotationCompletionProvider {
    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter) {
        if (annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return;
        }

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null) {
            return;
        }

        if(propertyName.equals("onDelete") && PhpLangUtil.equalsClassNames(annotationPropertyParameter.getPhpClass().getPresentableFQN(), "Doctrine\\ORM\\Mapping\\JoinColumn")) {
            for(String s: Arrays.asList("CASCADE", "SET NULL")) {
                completionParameter.getResult().addElement(LookupElementBuilder.create(s));
            }
        }
    }
}
