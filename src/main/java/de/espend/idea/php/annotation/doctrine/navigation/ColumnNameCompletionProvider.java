package de.espend.idea.php.annotation.doctrine.navigation;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.php.annotation.PhpAnnotationIcons;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
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

        if(propertyName.equals("name") && PhpLangUtil.equalsClassNames(annotationPropertyParameter.getPhpClass().getFQN(), "\\Doctrine\\ORM\\Mapping\\Column")) {
            PhpDocComment phpDocComment = PsiTreeUtil.getParentOfType(annotationPropertyParameter.getElement(), PhpDocComment.class);
            if (phpDocComment != null) {
                PhpPsiElement classField = phpDocComment.getNextPsiSibling();
                if (classField != null && classField.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS) {
                    PhpNamedElement phpNamedElement = PsiTreeUtil.getChildOfType(classField, PhpNamedElement.class);
                    if (phpNamedElement != null && StringUtils.isNotBlank(phpNamedElement.getName())) {
                        completionParameter.getResult().addElement(LookupElementBuilder.create(underscore(phpNamedElement.getName())).withIcon(PhpAnnotationIcons.DOCTRINE));
                    }
                }
            }

            PhpAttributesList parentOfType = PsiTreeUtil.getParentOfType(annotationPropertyParameter.getElement(), PhpAttributesList.class);
            if (parentOfType != null && parentOfType.getParent() instanceof PhpPsiElement phpPsiElement) {
                PhpNamedElement phpNamedElement = PsiTreeUtil.getChildOfType(phpPsiElement, PhpNamedElement.class);
                if (phpNamedElement != null && StringUtils.isNotBlank(phpNamedElement.getName())) {
                    completionParameter.getResult().addElement(LookupElementBuilder.create(underscore(phpNamedElement.getName())).withIcon(PhpAnnotationIcons.DOCTRINE));
                }
            }
        }
    }

    private String underscore(String s) {
        return StringUtils.capitalize(s).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

}
