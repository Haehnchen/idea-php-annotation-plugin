package de.espend.idea.php.annotation.extension;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.jetbrains.php.lang.PhpLangUtil;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.completion.parameter.CompletionParameter;

import java.util.Arrays;


public class DoctrineAnnotationStaticProvider implements PhpAnnotationCompletionProvider {

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null) {
            return;
        }

        if(propertyName.equals("type") && PhpLangUtil.equalsClassNames(annotationPropertyParameter.getPhpClass().getPresentableFQN(), "Doctrine\\ORM\\Mapping\\Column")) {
            for(String s: Arrays.asList("id", "string", "integer", "smallint", "bigint", "boolean", "decimal", "date", "time", "datetime", "text", "array", "float")) {
                completionParameter.getResult().addElement(LookupElementBuilder.create(s));
            }
        }

    }

}
