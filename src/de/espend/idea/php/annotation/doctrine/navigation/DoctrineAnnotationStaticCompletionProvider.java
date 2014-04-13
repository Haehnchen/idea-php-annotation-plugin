package de.espend.idea.php.annotation.doctrine.navigation;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.jetbrains.php.lang.PhpLangUtil;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;

import java.util.Arrays;


public class DoctrineAnnotationStaticCompletionProvider implements PhpAnnotationCompletionProvider {

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null) {
            return;
        }

        if(propertyName.equals("type") && PhpLangUtil.equalsClassNames(annotationPropertyParameter.getPhpClass().getPresentableFQN(), "Doctrine\\ORM\\Mapping\\Column")) {
            for(String s: Arrays.asList("id", "string", "integer", "smallint", "bigint", "boolean", "decimal", "date", "time", "datetime", "text", "array", "float")) {
                completionParameter.getResult().addElement(LookupElementBuilder.create(s));
            }
        }

        if(propertyName.equals("onDelete") && PhpLangUtil.equalsClassNames(annotationPropertyParameter.getPhpClass().getPresentableFQN(), "Doctrine\\ORM\\Mapping\\JoinColumn")) {
            for(String s: Arrays.asList("CASCADE", "SET NULL")) {
                completionParameter.getResult().addElement(LookupElementBuilder.create(s));
            }
        }

    }

}
