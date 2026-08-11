package de.espend.idea.php.annotation.doctrine.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.pattern.AnnotationPattern
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpElementsUtil

private val DOC_IDENTIFIER_PATTERN: ElementPattern<PsiElement> =
    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)

/**
 * Check for underlay class deprecations of Column type class from Doctrine
 *
 * Example:
 *  - '\Doctrine\ORM\Mapping\Column(type="json_array")'
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
open class DoctrineTypeDeprecatedInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        DoctrineTypePropertyVisitor(holder)

    private class DoctrineTypePropertyVisitor(
        private val holder: ProblemsHolder,
    ) : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            val stringLiteralExpression = element as? StringLiteralExpression ?: return
            val contents = getContentIfTypeValid(
                stringLiteralExpression,
                "\\Doctrine\\ORM\\Mapping\\Column",
                "type",
            )

            if (contents != null) {
                for (columnPhpClass in DoctrineUtil.getColumnTypesTargets(holder.project, contents)) {
                    if (!columnPhpClass.isDeprecated) {
                        continue
                    }

                    val deprecationMessage = PhpElementsUtil.getClassDeprecatedMessage(columnPhpClass)
                    holder.registerProblem(
                        element,
                        "[Annotations] " + (
                            deprecationMessage ?: "Field '$contents' is deprecated"
                        ),
                        ProblemHighlightType.LIKE_DEPRECATED,
                    )

                    break
                }
            }

            super.visitElement(element)
        }
    }
}

private fun getContentIfTypeValid(
    stringLiteralExpression: StringLiteralExpression,
    clazz: String,
    property: String,
): String? {
    if (AnnotationPattern.getAttributesValueReferencesPattern().accepts(stringLiteralExpression)) {
        val attributeNamePsi = PhpPsiUtil.getPrevSibling(
            stringLiteralExpression,
            { it is PsiWhiteSpace || it.node.elementType === PhpTokenTypes.opCOLON },
        )

        if (
            attributeNamePsi != null &&
            attributeNamePsi.node.elementType === PhpTokenTypes.IDENTIFIER &&
            property == attributeNamePsi.text
        ) {
            val phpAttribute = PsiTreeUtil.getParentOfType(stringLiteralExpression, PhpAttribute::class.java)
            if (phpAttribute != null && PhpLangUtil.equalsClassNames(clazz, phpAttribute.fqn)) {
                return stringLiteralExpression.contents
            }
        }
    } else if (stringLiteralExpression.node.elementType === PhpDocElementTypes.phpDocString) {
        val propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(
            stringLiteralExpression,
            DOC_IDENTIFIER_PATTERN,
        )

        if (propertyName != null && property == propertyName.text) {
            val phpDocTag = PsiTreeUtil.getParentOfType(stringLiteralExpression, PhpDocTag::class.java)
            if (phpDocTag != null) {
                val phpDocAnnotationContainer = AnnotationUtil.getPhpDocAnnotationContainer(phpDocTag)
                if (
                    phpDocAnnotationContainer != null &&
                    PhpLangUtil.equalsClassNames(phpDocAnnotationContainer.phpClass.fqn, clazz)
                ) {
                    return stringLiteralExpression.contents
                }
            }
        }
    }

    return null
}
