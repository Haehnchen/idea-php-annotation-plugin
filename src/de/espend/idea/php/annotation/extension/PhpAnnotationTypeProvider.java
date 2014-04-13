package de.espend.idea.php.annotation.extension;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.completion.parameter.CompletionParameter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PhpAnnotationTypeProvider implements PhpAnnotationCompletionProvider {

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();

        if(!annotationPropertyParameter.getType().equals(AnnotationPropertyParameter.Type.STRING) && propertyName == null) {
            return;
        }

        Set<String> values = new HashSet<String>();
        for(Field field: annotationPropertyParameter.getPhpClass().getFields()) {
            if(field.getName().equals(propertyName)) {

                String typeName = field.getType().toString();
                if(typeName.equals("bool") || typeName.equals("boolean")) {
                    values.addAll(Arrays.asList("false", "true"));
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
        }

        for(String s: values) {
            completionParameter.getResult().addElement(LookupElementBuilder.create(s));
        }

    }

}
