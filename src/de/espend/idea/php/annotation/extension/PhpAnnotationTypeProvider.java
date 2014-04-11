package de.espend.idea.php.annotation.extension;

import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationExtension;
import de.espend.idea.php.annotation.completion.parameter.CompletionParameter;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PhpAnnotationTypeProvider implements PhpAnnotationExtension {

    @Nullable
    @Override
    public Collection<String> getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();

        if(!annotationPropertyParameter.getType().equals(AnnotationPropertyParameter.Type.STRING) && propertyName == null) {
            return null;
        }

        Set<String> values = new HashSet<String>();
        for(Field field: annotationPropertyParameter.getPhpClass().getFields()) {
            if(field.getName().equals(propertyName)) {

                if(field.getType().toString().equals("bool")) {
                    values.addAll(Arrays.asList("false", "true"));
                }

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

        return values;
    }

    @Nullable
    @Override
    public Collection<PsiReference> getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter) {
        return null;
    }
}
