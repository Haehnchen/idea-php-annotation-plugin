package de.espend.idea.php.annotation.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpAnnotationTypeCompletionProvider implements PhpAnnotationCompletionProvider {

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter) {
        String propertyName = annotationPropertyParameter.getPropertyName();
        if(!annotationPropertyParameter.getType().equals(AnnotationPropertyParameter.Type.PROPERTY_VALUE) && propertyName == null) {
            return;
        }

        Set<String> values = new HashSet<>();
        for(Field field: annotationPropertyParameter.getPhpClass().getFields()) {
            if(!field.getName().equals(propertyName)) {
                continue;
            }

            for (String s : field.getType().getTypes()) {
                // _BOOLEAN, _BOOL are private so rebuild a boolean check
                s = StringUtils.stripStart(s,"\\");
                if(s.equalsIgnoreCase("bool") || s.equalsIgnoreCase("boolean")) {
                    values.addAll(Arrays.asList("false", "true"));

                    // stop on first match
                    break;
                }
            }

            // @Enum({"AUTO", "SEQUENCE"})
            PhpDocComment docComment = field.getDocComment();
            if(docComment != null) {
                PhpDocTag[] phpDocTags = docComment.getTagElementsByName("@Enum");
                for(PhpDocTag phpDocTag: phpDocTags) {
                    PhpPsiElement phpDocAttrList = phpDocTag.getFirstPsiChild();
                    if(phpDocAttrList != null) {
                        String enumArrayString = phpDocAttrList.getText();
                        Pattern targetPattern = Pattern.compile("\"(\\w+)\"");

                        Matcher matcher = targetPattern.matcher(enumArrayString);
                        while (matcher.find()) {
                            values.add(matcher.group(1));
                        }
                    }
                }
            }
        }

        for(String s: values) {
            completionParameter.getResult().addElement(LookupElementBuilder.create(s));
        }
    }
}
