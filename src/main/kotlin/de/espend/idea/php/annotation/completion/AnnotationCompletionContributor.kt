package de.espend.idea.php.annotation.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Key
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIcons
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.inspections.attributes.PhpClassCantBeUsedAsAttributeInspection
import com.jetbrains.php.lang.inspections.attributes.PhpInapplicableAttributeTargetDeclarationInspection
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpAttributesList
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.PhpUse
import com.jetbrains.php.lang.psi.elements.PhpUseList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.stubs.indexes.PhpAttributesFQNsIndex
import de.espend.idea.php.annotation.ApplicationSettings
import de.espend.idea.php.annotation.completion.insert.AnnotationTagInsertHandler
import de.espend.idea.php.annotation.completion.insert.AttributeAliasInsertHandler
import de.espend.idea.php.annotation.completion.lookupelements.PhpAnnotationPropertyLookupElement
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement
import de.espend.idea.php.annotation.dict.AnnotationProperty
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum
import de.espend.idea.php.annotation.dict.AnnotationTarget
import de.espend.idea.php.annotation.dict.PhpAnnotation
import de.espend.idea.php.annotation.dict.UseAliasOption
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyCompletionParameter
import de.espend.idea.php.annotation.pattern.AnnotationPattern
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpElementsUtil
import de.espend.idea.php.annotation.util.PhpIndexUtil
import org.apache.commons.lang3.StringUtils
import java.util.Collections

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationCompletionContributor : CompletionContributor() {
    init {
        // @<caret>, * @<caret>
        extend(CompletionType.BASIC, AnnotationPattern.getDocBlockTag(), PhpDocBlockTagAnnotations())

        // #[<caret>] but only provide alias feature
        extend(CompletionType.BASIC, AnnotationPattern.getAttributeNamePattern(), PhpAttributeAlias())

        // @Callback("", <caret>)
        extend(CompletionType.BASIC, AnnotationPattern.getDocAttribute(), PhpDocAttributeList())
        extend(CompletionType.BASIC, AnnotationPattern.getTextIdentifier(), PhpDocAttributeValue())
        extend(CompletionType.BASIC, AnnotationPattern.getDefaultPropertyValue(), PhpDocDefaultValue())
        extend(CompletionType.BASIC, AnnotationPattern.getDocBlockTagAfterBackslash(), PhpDocBlockTagAlias())

        // @Route(name=ClassName::<FOO>)
        extend(CompletionType.BASIC, AnnotationPattern.getClassConstant(), PhpDocClassConstantCompletion())

        // @Foo(name={"FOOBAR", "<caret>"})
        extend(CompletionType.BASIC, AnnotationPattern.getPropertyArrayPattern(), PhpDocArrayPropertyCompletion())

        // #[Route('/path', methods: ['action'])]
        extend(CompletionType.BASIC, AnnotationPattern.getAttributesArrayPattern(), AttributesArrayPropertyCompletion())

        // #[Route('/path', methods: 'action')]
        extend(CompletionType.BASIC, AnnotationPattern.getAttributesValuePattern(), AttributesValuePropertyCompletion())

        // #[Route('<caret>')]
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(AnnotationPattern.getAttributesDefaultPattern()),
            AttributeDefaultValue(),
        )
    }

    private inner class PhpDocDefaultValue : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val phpDocTag = PsiTreeUtil.getParentOfType(position, PhpDocTag::class.java)
            val phpClass = AnnotationUtil.getAnnotationReference(phpDocTag) ?: return
            val property = AnnotationPropertyParameter(position, phpClass, AnnotationPropertyParameter.Type.DEFAULT)
            providerWalker(parameters, context, result, property)
        }
    }

    private inner class AttributeDefaultValue : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val parent = position.parent
            if (parent !is StringLiteralExpression) {
                return
            }

            val phpAttribute = PsiTreeUtil.getParentOfType(parent, PhpAttribute::class.java) ?: return
            val fqn = phpAttribute.fqn ?: return
            val phpClass = PhpElementsUtil.getClassInterface(position.project, fqn) ?: return
            val property = AnnotationPropertyParameter(position, phpClass, AnnotationPropertyParameter.Type.DEFAULT)
            providerWalker(parameters, context, result, property)
        }
    }

    private inner class PhpDocArrayPropertyCompletion : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val parent = position.parent
            if (parent !is StringLiteralExpression) {
                return
            }

            val phpDocTag = PsiTreeUtil.getParentOfType(position, PhpDocTag::class.java)
            val phpClass = AnnotationUtil.getAnnotationReference(phpDocTag) ?: return
            val propertyForEnum = AnnotationUtil.getPropertyForArray(parent) ?: return
            val property = AnnotationPropertyParameter(
                position,
                phpClass,
                propertyForEnum.text,
                AnnotationPropertyParameter.Type.PROPERTY_ARRAY,
            )
            providerWalker(parameters, context, result, property)
        }
    }

    private class AttributesArrayPropertyCompletion : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            if (position.parent !is StringLiteralExpression) {
                return
            }

            val array = PsiTreeUtil.getParentOfType(position, ArrayCreationExpression::class.java) ?: return
            val attributeNamePsi = PhpPsiUtil.getPrevSibling(
                array,
                Condition<PsiElement> { sibling ->
                    sibling is PsiWhiteSpace || sibling.node.elementType === PhpTokenTypes.opCOLON
                },
            )
            if (attributeNamePsi == null || attributeNamePsi.node.elementType !== PhpTokenTypes.IDENTIFIER) {
                return
            }

            val attributeName = attributeNamePsi.text
            if (StringUtils.isBlank(attributeName)) {
                return
            }

            val phpAttribute = PsiTreeUtil.getParentOfType(position, PhpAttribute::class.java) ?: return
            val fqn = phpAttribute.fqn ?: return
            val phpClass = PhpElementsUtil.getClassInterface(position.project, fqn) ?: return
            val completionParameter = AnnotationCompletionProviderParameter(parameters, context, result)
            val property = AnnotationPropertyParameter(
                position,
                phpClass,
                attributeName,
                AnnotationPropertyParameter.Type.PROPERTY_ARRAY,
            )

            for (extension in AnnotationUtil.EXTENSION_POINT_COMPLETION.extensions) {
                extension.getPropertyValueCompletions(property, completionParameter)
            }
        }
    }

    private class AttributesValuePropertyCompletion : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val parent = position.parent
            if (parent !is StringLiteralExpression) {
                return
            }

            val attributeNamePsi = PhpPsiUtil.getPrevSibling(
                parent,
                Condition<PsiElement> { sibling ->
                    sibling is PsiWhiteSpace || sibling.node.elementType === PhpTokenTypes.opCOLON
                },
            )
            if (attributeNamePsi == null || attributeNamePsi.node.elementType !== PhpTokenTypes.IDENTIFIER) {
                return
            }

            val attributeName = attributeNamePsi.text
            if (StringUtils.isBlank(attributeName)) {
                return
            }

            val phpAttribute = PsiTreeUtil.getParentOfType(position, PhpAttribute::class.java) ?: return
            val fqn = phpAttribute.fqn ?: return
            val phpClass = PhpElementsUtil.getClassInterface(position.project, fqn) ?: return
            val completionParameter = AnnotationCompletionProviderParameter(parameters, context, result)
            val property = AnnotationPropertyParameter(
                position,
                phpClass,
                attributeName,
                AnnotationPropertyParameter.Type.PROPERTY_VALUE,
            )

            for (extension in AnnotationUtil.EXTENSION_POINT_COMPLETION.extensions) {
                extension.getPropertyValueCompletions(property, completionParameter)
            }
        }
    }

    private fun providerWalker(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
        property: AnnotationPropertyParameter,
    ) {
        val completionParameter = AnnotationCompletionProviderParameter(parameters, context, result)
        for (extension in AnnotationUtil.EXTENSION_POINT_COMPLETION.extensions) {
            extension.getPropertyValueCompletions(property, completionParameter)
        }
    }

    private inner class PhpDocAttributeValue : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val phpDocString = position.context
            if (phpDocString !is StringLiteralExpression) {
                return
            }

            val propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(phpDocString, DOC_IDENTIFIER_PATTERN)
                ?: return
            val phpDocTag = PsiTreeUtil.getParentOfType(position, PhpDocTag::class.java)
            val phpClass = AnnotationUtil.getAnnotationReference(phpDocTag) ?: return
            val property = AnnotationPropertyParameter(
                position,
                phpClass,
                propertyName.text,
                AnnotationPropertyParameter.Type.PROPERTY_VALUE,
            )
            providerWalker(parameters, context, result, property)
        }
    }

    private class PhpDocAttributeList : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val phpDocTag = PsiTreeUtil.getParentOfType(position, PhpDocTag::class.java) ?: return
            val phpClass = AnnotationUtil.getAnnotationReference(phpDocTag) ?: return

            AnnotationUtil.visitAttributes(phpClass) { attributeName, type, _ ->
                result.addElement(
                    PhpAnnotationPropertyLookupElement(
                        AnnotationProperty(attributeName, AnnotationPropertyEnum.fromString(type)),
                    ),
                )
                null
            }

            var virtualPropertyParameter: AnnotationVirtualPropertyCompletionParameter? = null
            var completionParameter: AnnotationCompletionProviderParameter? = null

            for (extension in AnnotationUtil.EP_VIRTUAL_PROPERTIES.extensions) {
                if (virtualPropertyParameter == null) {
                    virtualPropertyParameter = AnnotationVirtualPropertyCompletionParameter(phpClass)
                }
                if (completionParameter == null) {
                    completionParameter = AnnotationCompletionProviderParameter(parameters, context, result)
                }
                extension.addCompletions(virtualPropertyParameter, completionParameter)
            }

            val lookupElements = virtualPropertyParameter?.lookupElements ?: return
            for ((name, type) in lookupElements) {
                result.addElement(PhpAnnotationPropertyLookupElement(AnnotationProperty(name, type)))
            }
        }
    }

    private class PhpAttributeAlias : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val attributesList = PsiTreeUtil.getParentOfType(position, PhpAttributesList::class.java) ?: return
            attachLookupElements(position.project, attributesList, result)
        }

        private fun attachLookupElements(
            project: Project,
            attributesList: PhpAttributesList,
            result: CompletionResultSet,
        ) {
            val items = HashMap<String, String>()
            for (option in ApplicationSettings.getUseAliasOptionsWithDefaultFallback()) {
                val alias = option.alias ?: continue
                val className = option.className ?: continue
                items[alias] = className
            }
            items.putAll(getUseAsMap(attributesList))

            val attributesByNamespace = getAttributeFqnsByNamespace(project)
            for ((alias, aliasFqn) in items) {
                val namespace = "\\" + StringUtils.stripStart(aliasFqn, "\\")
                val fqns = attributesByNamespace[namespace] ?: continue

                for (fqnClass in fqns) {
                    val lookupString = alias + "\\" + fqnClass.substring(namespace.length + 1)
                    val underlyingClass = PhpElementsUtil.getClassInterface(project, fqnClass) ?: continue
                    val rootAttributes = PhpClassCantBeUsedAsAttributeInspection.rootAttributes(underlyingClass).toList()
                    if (
                        rootAttributes.isNotEmpty() &&
                        PhpInapplicableAttributeTargetDeclarationInspection.getInapplicableDeclarationName(
                            attributesList.parent,
                            rootAttributes,
                        ) == null
                    ) {
                        val lookupElement = PhpClassAnnotationLookupElement(
                            underlyingClass,
                            UseAliasOption(aliasFqn, alias, true),
                            lookupString,
                        )
                        lookupElement.withInsertHandler(AttributeAliasInsertHandler.getInstance())
                        result.addElement(
                            if (underlyingClass.isDeprecated) {
                                PrioritizedLookupElement.withPriority(lookupElement, -1000.0)
                            } else {
                                lookupElement
                            },
                        )
                    }
                }
            }
        }
    }

    private class PhpDocBlockTagAnnotations : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val docComment = PsiTreeUtil.getParentOfType(position, PhpDocComment::class.java) ?: return
            val annotationTarget = PhpElementsUtil.findAnnotationTarget(docComment) ?: return
            val importMap = AnnotationUtil.getUseImportMap(docComment as PsiElement)
            attachLookupElements(parameters.position.project, importMap, annotationTarget, result)
        }

        private fun attachLookupElements(
            project: Project,
            importMap: Map<String, String>,
            target: AnnotationTarget,
            result: CompletionResultSet,
        ) {
            for (annotation in getPhpAnnotationTargetClasses(project, target)) {
                val underlyingClass = annotation.phpClass
                var fqnClass = underlyingClass.fqn
                if (!fqnClass.startsWith("\\")) {
                    fqnClass = "\\$fqnClass"
                }

                val lookupElement = PhpClassAnnotationLookupElement(underlyingClass)
                    .withInsertHandler(AnnotationTagInsertHandler.getInstance())

                for ((alias, importedFqn) in importMap) {
                    if (fqnClass.startsWith("$importedFqn\\")) {
                        lookupElement.withTypeText(alias + fqnClass.substring(importedFqn.length))
                    }
                }

                for (option in ApplicationSettings.getUseAliasOptionsWithDefaultFallback()) {
                    val className = option.className ?: continue
                    val alias = option.alias ?: continue
                    val namespace = "\\" + StringUtils.stripStart(className, "\\") + "\\"
                    if (!fqnClass.startsWith(namespace)) {
                        continue
                    }

                    val aliasLookup = PhpClassAnnotationLookupElement(
                        underlyingClass,
                        option,
                        alias + "\\" + fqnClass.substring(namespace.length),
                    )
                    aliasLookup.withInsertHandler(AnnotationTagInsertHandler.getInstance())
                    result.addElement(
                        if (underlyingClass.isDeprecated) {
                            PrioritizedLookupElement.withPriority(aliasLookup, -1000.0)
                        } else {
                            aliasLookup
                        },
                    )
                }

                result.addElement(
                    if (underlyingClass.isDeprecated) {
                        PrioritizedLookupElement.withPriority(lookupElement, -1000.0)
                    } else {
                        lookupElement
                    },
                )
            }
        }

        private fun getPhpAnnotationTargetClasses(
            project: Project,
            target: AnnotationTarget,
        ): Collection<PhpAnnotation> {
            return AnnotationUtil.getAnnotationsOnTargetMap(
                project,
                target,
                AnnotationTarget.ALL,
                AnnotationTarget.UNKNOWN,
                AnnotationTarget.UNDEFINED,
            ).values
        }
    }

    private class PhpDocBlockTagAlias : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val phpDocTag = position.parent
            if (phpDocTag !is PhpDocTag) {
                return
            }

            var name = phpDocTag.name
            if (!name.startsWith("@")) {
                return
            }
            val separator = name.indexOf("\\")
            if (separator == -1) {
                return
            }
            name = name.substring(1, separator)

            val importMap = AnnotationUtil.getUseImportMap(phpDocTag as PsiElement)
            var namespace = importMap[name] ?: return
            var annotationTarget = PhpElementsUtil.findAnnotationTarget(
                PsiTreeUtil.getParentOfType(position, PhpDocComment::class.java),
            )
            if (annotationTarget == null) {
                annotationTarget = AnnotationTarget.UNKNOWN
            }
            if (!namespace.startsWith("\\")) {
                namespace = "\\$namespace"
            }

            val annotations = AnnotationUtil.getAnnotationsOnTargetMap(
                position.project,
                AnnotationTarget.ALL,
                AnnotationTarget.UNDEFINED,
                AnnotationTarget.UNKNOWN,
                annotationTarget,
            )

            for (phpClass in PhpIndexUtil.getPhpClassInsideNamespace(position.project, namespace)) {
                val fqnName = StringUtils.stripStart(phpClass.fqn, "\\")
                val annotation = annotations[fqnName]
                if (
                    annotation != null &&
                    annotation.matchOneOf(
                        AnnotationTarget.ALL,
                        AnnotationTarget.UNDEFINED,
                        AnnotationTarget.UNKNOWN,
                        annotationTarget,
                    )
                ) {
                    val lookupString = name + "\\" + fqnName.substring(namespace.length)
                    result.addElement(
                        LookupElementBuilder.create(lookupString)
                            .withTypeText(phpClass.presentableFQN, true)
                            .withIcon(phpClass.icon)
                            .withInsertHandler(AnnotationTagInsertHandler.getInstance()),
                    )
                }
            }
        }
    }

    private class PhpDocClassConstantCompletion : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val position = parameters.originalPosition ?: return
            val phpClass = AnnotationUtil.getClassFromConstant(position) ?: return

            for (field in phpClass.fields) {
                if (field.isConstant) {
                    result.addElement(
                        LookupElementBuilder.create(field.name)
                            .withIcon(PhpIcons.FIELD)
                            .withTypeText(phpClass.name, true),
                    )
                }
            }
        }
    }

    private companion object {
        val ATTRIBUTE_FQNS_BY_NAMESPACE_CACHE: Key<CachedValue<Map<String, Collection<String>>>> =
            Key.create("ATTRIBUTE_FQNS_BY_NAMESPACE_CACHE")

        val DOC_IDENTIFIER_PATTERN: ElementPattern<PsiElement> =
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)

        fun getAttributeFqnsByNamespace(project: Project): Map<String, Collection<String>> {
            return CachedValuesManager.getManager(project).getCachedValue(
                project,
                ATTRIBUTE_FQNS_BY_NAMESPACE_CACHE,
                {
                    val items = HashMap<String, MutableList<String>>()
                    for (indexedFqn in FileBasedIndex.getInstance().getAllKeys(PhpAttributesFQNsIndex.KEY, project)) {
                        var fqnClass = indexedFqn
                        if (!fqnClass.startsWith("\\")) {
                            fqnClass = "\\$fqnClass"
                        }

                        var index = fqnClass.indexOf("\\", 1)
                        while (index > 0) {
                            items.computeIfAbsent(fqnClass.substring(0, index)) { ArrayList() }.add(fqnClass)
                            index = fqnClass.indexOf("\\", index + 1)
                        }
                    }

                    val immutableItems = HashMap<String, Collection<String>>()
                    for ((namespace, fqns) in items) {
                        immutableItems[namespace] = Collections.unmodifiableList(ArrayList(fqns))
                    }

                    CachedValueProvider.Result.create(
                        Collections.unmodifiableMap(immutableItems),
                        AnnotationUtil.getModificationTrackerForIndexId(project, PhpAttributesFQNsIndex.KEY),
                    )
                },
                false,
            )
        }

        fun getUseAsMap(element: PsiElement): Map<String, String> {
            val scope: PhpPsiElement = PhpCodeInsightUtil.findScopeForUseOperator(element) ?: return emptyMap()
            val imports = HashMap<String, String>()
            for (useList: PhpUseList in PhpCodeInsightUtil.collectImports(scope)) {
                for (use: PhpUse in useList.declarations) {
                    val alias = use.aliasName ?: continue
                    imports[alias] = use.fqn
                }
            }
            return imports
        }
    }
}
