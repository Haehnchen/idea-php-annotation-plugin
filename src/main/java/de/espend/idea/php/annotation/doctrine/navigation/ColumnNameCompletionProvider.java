package de.espend.idea.php.annotation.doctrine.navigation;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.PhpAttributesList;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.PhpPromotedFieldParameterImpl;
import de.espend.idea.php.annotation.PhpAnnotationIcons;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import org.apache.commons.lang3.StringUtils;

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
        if (!"name".equals(propertyName)) {
            return;
        }

        String fqn = annotationPropertyParameter.getPhpClass().getFQN();
        boolean isColumn = PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\Column")
            || PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\JoinColumn")
            || PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\InverseJoinColumn");

        if (isColumn) {
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
                PhpNamedElement phpNamedElement = phpPsiElement instanceof PhpPromotedFieldParameterImpl phpPromotedFieldParameter
                    ? phpPromotedFieldParameter
                    : PsiTreeUtil.getChildOfType(phpPsiElement, PhpNamedElement.class);

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
