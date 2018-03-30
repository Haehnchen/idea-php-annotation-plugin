package de.espend.idea.php.annotation.doctrine.navigation;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import com.jetbrains.php.lang.psi.elements.Field;
import org.apache.commons.lang.StringUtils;

/**
 * "Column(name="field_data2", type="integer")"
 * private $FieldData2;
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ColumnNameCompletionProvider implements PhpAnnotationCompletionProvider {

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, AnnotationCompletionProviderParameter completionParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null) {
            return;
        }

        if(propertyName.equals("name") && PhpLangUtil.equalsClassNames(annotationPropertyParameter.getPhpClass().getPresentableFQN(), "Doctrine\\ORM\\Mapping\\Column")) {
            PhpDocComment phpDocComment = PsiTreeUtil.getParentOfType(annotationPropertyParameter.getElement(), PhpDocComment.class);
            if(phpDocComment != null) {
                PhpPsiElement classField = phpDocComment.getNextPsiSibling();
                if(classField != null && classField.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS) {
                    Field field = PsiTreeUtil.getChildOfType(classField, Field.class);
                    if(field != null) {
                        String name = field.getName();
                        if(StringUtils.isNotBlank(name)) {
                            completionParameter.getResult().addElement(LookupElementBuilder.create(underscore(name)));
                        }
                    }
                }
            }
        }

    }

    private String underscore(String s) {
        return StringUtils.capitalize(s).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

}
