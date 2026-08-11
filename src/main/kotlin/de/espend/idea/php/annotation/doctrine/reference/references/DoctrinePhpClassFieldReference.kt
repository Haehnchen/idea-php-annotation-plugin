package de.espend.idea.php.annotation.doctrine.reference.references

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpAttributesList
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.impl.PhpPromotedFieldParameterImpl
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpElementsUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DoctrinePhpClassFieldReference(
    psiElement: StringLiteralExpression,
    private val phpClass: PhpClass,
) : PsiPolyVariantReferenceBase<PsiElement>(psiElement) {
    private val content = psiElement.contents

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val fields = phpClass.fields.filter { field -> !field.isConstant && content == field.name }
        return PsiElementResolveResult.createResults(fields)
    }

    override fun getVariants(): Array<Any> {
        val entity = PsiTreeUtil.getParentOfType(element, PhpClass::class.java)
        return phpClass.fields
            .asSequence()
            .filterNot { field -> field.isConstant }
            .map { field -> attachLookupInformation(field, LookupElementBuilder.createWithIcon(field), entity) }
            .toList()
            .toTypedArray()
    }

    private fun attachLookupInformation(
        field: Field,
        lookupElement: LookupElementBuilder,
        entity: PhpClass?,
    ): LookupElementBuilder {
        var result = lookupElement
        val matchForeignType = entity != null && fieldMatchesEntity(field.project, field, entity.fqn)

        // get some more presentable completion information
        val docBlock = field.docComment
        if (docBlock != null) {
            val annotationContainer = AnnotationUtil.getPhpDocCommentAnnotationContainer(docBlock) ?: return result

            // search column type
            annotationContainer.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Column")
                ?.getPropertyValue("type")
                ?.let { value -> result = result.withTypeText(value, true) }

            // search for relations
            val relation = annotationContainer.getFirstPhpDocBlock(*DoctrineUtil.DOCTRINE_RELATION_FIELDS)
            if (relation != null) {
                result = attachRelationInformation(result, relation, matchForeignType)
            }

            return result
        }

        val attributeLists = when {
            field is PhpPromotedFieldParameterImpl ->
                PsiTreeUtil.getChildrenOfTypeAsList(field, PhpAttributesList::class.java)

            field.parent is PhpPsiElement ->
                PsiTreeUtil.getChildrenOfTypeAsList(field.parent, PhpAttributesList::class.java)

            else -> emptyList()
        }

        for (attributesList in attributeLists) {
            for (attribute in attributesList.getAttributes("\\Doctrine\\ORM\\Mapping\\Column")) {
                PhpElementsUtil.getAttributeArgumentStringByName(attribute, "type")
                    ?.let { value -> result = result.withTypeText(value, true) }
            }

            for (relationClass in DoctrineUtil.DOCTRINE_RELATION_FIELDS) {
                for (attribute in attributesList.getAttributes(relationClass)) {
                    result = attachRelationInformation(result, attribute, matchForeignType)
                }
            }
        }

        return result
    }

    private fun fieldMatchesEntity(project: Project, field: Field, entityFqn: String): Boolean =
        PhpIndex.getInstance(project)
            .completeType(project, field.type, HashSet())
            .types
            .any { type -> type == entityFqn || type == "$entityFqn[]" }

    private fun attachRelationInformation(
        lookupElement: LookupElementBuilder,
        relation: PhpDocTagAnnotation,
        matchForeignType: Boolean,
    ): LookupElementBuilder {
        var result = lookupElement
        relation.getPropertyValue("targetEntity")?.let { value ->
            result = result
                .withTypeText(StringUtils.stripStart(value, "\\"), true)
                .withBoldness(matchForeignType)
        }

        return result.withTailText("(${relation.phpClass.name})", true)
    }

    private fun attachRelationInformation(
        lookupElement: LookupElementBuilder,
        relation: PhpAttribute,
        matchForeignType: Boolean,
    ): LookupElementBuilder {
        val value = PhpElementsUtil.getAttributeArgumentStringByName(relation, "targetEntity")
            ?: return lookupElement

        return lookupElement
            .withTypeText(StringUtils.stripStart(value, "\\"), true)
            .withBoldness(matchForeignType)
    }
}
