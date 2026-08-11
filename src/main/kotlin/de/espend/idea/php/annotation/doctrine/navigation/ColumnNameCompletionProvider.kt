package de.espend.idea.php.annotation.doctrine.navigation

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.parser.PhpElementTypes
import com.jetbrains.php.lang.psi.elements.PhpAttributesList
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.impl.PhpPromotedFieldParameterImpl
import de.espend.idea.php.annotation.PhpAnnotationIcons
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import org.apache.commons.lang3.StringUtils
import java.util.Locale

/**
 * "Column(name="field_data2", type="integer")"
 * private $FieldData2;
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class ColumnNameCompletionProvider : PhpAnnotationCompletionProvider {
    override fun getPropertyValueCompletions(
        annotationPropertyParameter: AnnotationPropertyParameter,
        completionParameter: AnnotationCompletionProviderParameter
    ) {
        val propertyName = annotationPropertyParameter.propertyName
        if ("name" != propertyName) {
            return
        }

        val fqn = annotationPropertyParameter.phpClass.fqn
        val isColumn = PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\Column")
                || PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\JoinColumn")
                || PhpLangUtil.equalsClassNames(fqn, "\\Doctrine\\ORM\\Mapping\\InverseJoinColumn")

        if (isColumn) {
            val phpDocComment = PsiTreeUtil.getParentOfType<PhpDocComment?>(
                annotationPropertyParameter.element,
                PhpDocComment::class.java
            )
            if (phpDocComment != null) {
                val classField = phpDocComment.nextPsiSibling
                if (classField != null && classField.node.elementType === PhpElementTypes.CLASS_FIELDS) {
                    val phpNamedElement =
                        PsiTreeUtil.getChildOfType(classField, PhpNamedElement::class.java)
                    if (phpNamedElement != null && StringUtils.isNotBlank(phpNamedElement.name)) {
                        completionParameter.result.addElement(
                            LookupElementBuilder.create(underscoreColumnName(phpNamedElement.name)).withIcon(
                                PhpAnnotationIcons.DOCTRINE
                            )
                        )
                    }
                }
            }

            val parentOfType = PsiTreeUtil.getParentOfType<PhpAttributesList?>(
                annotationPropertyParameter.element,
                PhpAttributesList::class.java
            )
            val phpPsiElement = parentOfType?.parent as? PhpPsiElement
            if (phpPsiElement != null) {
                val phpNamedElement = phpPsiElement as? PhpPromotedFieldParameterImpl
                    ?: PsiTreeUtil.getChildOfType(phpPsiElement, PhpNamedElement::class.java)

                if (phpNamedElement != null && StringUtils.isNotBlank(phpNamedElement.name)) {
                    completionParameter.result.addElement(
                        LookupElementBuilder.create(underscoreColumnName(phpNamedElement.name)).withIcon(
                            PhpAnnotationIcons.DOCTRINE
                        )
                    )
                }
            }
        }
    }
}

private fun underscoreColumnName(name: String): String =
    StringUtils.capitalize(name).replace("([a-z])([A-Z])".toRegex(), "$1_$2").lowercase(Locale.getDefault())
