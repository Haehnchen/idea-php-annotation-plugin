package de.espend.idea.php.annotation.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.inspections.attributes.PhpClassCantBeUsedAsAttributeInspection;
import com.jetbrains.php.lang.inspections.attributes.PhpInapplicableAttributeTargetDeclarationInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpAttributesFQNsIndex;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.completion.insert.AnnotationTagInsertHandler;
import de.espend.idea.php.annotation.completion.insert.AttributeAliasInsertHandler;
import de.espend.idea.php.annotation.completion.lookupelements.PhpAnnotationPropertyLookupElement;
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement;
import de.espend.idea.php.annotation.dict.*;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.PhpAnnotationVirtualProperties;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationVirtualPropertyCompletionParameter;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.annotation.util.PhpIndexUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationCompletionContributor extends CompletionContributor {

    public AnnotationCompletionContributor() {

        // @<caret>
        // * @<caret>
        extend(CompletionType.BASIC, AnnotationPattern.getDocBlockTag(), new PhpDocBlockTagAnnotations());

        // #[<caret>] but only provide alias feature
        extend(CompletionType.BASIC, AnnotationPattern.getAttributeNamePattern(), new PhpAttributeAlias());

        // @Callback("", <caret>)
        extend(CompletionType.BASIC, AnnotationPattern.getDocAttribute(), new PhpDocAttributeList());
        extend(CompletionType.BASIC, AnnotationPattern.getTextIdentifier(), new PhpDocAttributeValue());
        extend(CompletionType.BASIC, AnnotationPattern.getDefaultPropertyValue(), new PhpDocDefaultValue());
        extend(CompletionType.BASIC, AnnotationPattern.getDocBlockTagAfterBackslash(), new PhpDocBlockTagAlias());

        // @Route(name=ClassName::<FOO>)
        extend(CompletionType.BASIC, AnnotationPattern.getClassConstant(), new PhpDocClassConstantCompletion());

        // @Foo(name={"FOOBAR", "<caret>"})
        extend(CompletionType.BASIC, AnnotationPattern.getPropertyArrayPattern(), new PhpDocArrayPropertyCompletion());

        // #[Route('/path', methods: ['action'])]
        extend(CompletionType.BASIC, AnnotationPattern.getAttributesArrayPattern(), new AttributesArrayPropertyCompletion());

        // #[Route('/path', methods: 'action')]
        extend(CompletionType.BASIC, AnnotationPattern.getAttributesValuePattern(), new AttributesValuePropertyCompletion());

        // #[Route('<caret>')]
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(AnnotationPattern.getAttributesDefaultPattern()), new AttributeDefaultValue());
    }

    private class PhpDocDefaultValue extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement psiElement = parameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PhpDocTag.class);
            PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
            if(phpClass == null) {
                return;
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(parameters.getOriginalPosition(), phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            providerWalker(parameters, context, result, annotationPropertyParameter);

        }
    }

    private class AttributeDefaultValue extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement psiElement = parameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PsiElement parent = psiElement.getParent();
            if(!(parent instanceof StringLiteralExpression)) {
                return;
            }

            PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(parent, PhpAttribute.class);
            if (phpAttribute == null) {
                return;
            }

            String fqn = phpAttribute.getFQN();
            if (fqn == null) {
                return;
            }

            PhpClass phpClass = PhpElementsUtil.getClassInterface(psiElement.getProject(), fqn);
            if (phpClass == null) {
                return;
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(parameters.getOriginalPosition(), phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            providerWalker(parameters, context, result, annotationPropertyParameter);
        }
    }

    /**
     * "@FOO(property={"FOO", "FOO"})"
     */
    private class PhpDocArrayPropertyCompletion extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement psiElement = parameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PsiElement parent = psiElement.getParent();
            if(!(parent instanceof StringLiteralExpression)) {
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PhpDocTag.class);
            PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
            if(phpClass == null) {
                return;
            }

            PsiElement propertyForEnum = AnnotationUtil.getPropertyForArray((StringLiteralExpression) parent);
            if(propertyForEnum == null) {
                return;
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(
                parameters.getOriginalPosition(),
                phpClass,
                propertyForEnum.getText(),
                AnnotationPropertyParameter.Type.PROPERTY_ARRAY
            );

            providerWalker(parameters, context, result, annotationPropertyParameter);
        }
    }

    /**
     * #[Route('/path', methods: ['action'])]
     */
    private static class AttributesArrayPropertyCompletion extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement psiElement = parameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PsiElement parent = psiElement.getParent();
            if(!(parent instanceof StringLiteralExpression)) {
                return;
            }

            ArrayCreationExpression arrayCreationExpression = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), ArrayCreationExpression.class);
            if (arrayCreationExpression == null) {
                return;
            }

            PsiElement attributeNamePsi = PhpPsiUtil.getPrevSibling(arrayCreationExpression, psiElement1 -> psiElement1 instanceof PsiWhiteSpace || psiElement1.getNode().getElementType() == PhpTokenTypes.opCOLON);
            if (attributeNamePsi == null || attributeNamePsi.getNode().getElementType() != PhpTokenTypes.IDENTIFIER) {
                return;
            }

            String attributeName = attributeNamePsi.getText();
            if (StringUtils.isBlank(attributeName)) {
                return;
            }

            PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PhpAttribute.class);
            if (phpAttribute == null) {
                return;
            }

            String fqn = phpAttribute.getFQN();
            if (fqn == null) {
                return;
            }

            PhpClass phpClass = PhpElementsUtil.getClassInterface(psiElement.getProject(), fqn);
            if (phpClass == null) {
                return;
            }

            AnnotationCompletionProviderParameter completionParameter = new AnnotationCompletionProviderParameter(parameters, context, result);

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(
                parameters.getOriginalPosition(),
                phpClass,
                attributeName,
                AnnotationPropertyParameter.Type.PROPERTY_ARRAY
            );

            for(PhpAnnotationCompletionProvider phpAnnotationExtension : AnnotationUtil.EXTENSION_POINT_COMPLETION.getExtensions()) {
                phpAnnotationExtension.getPropertyValueCompletions(annotationPropertyParameter, completionParameter);
            }
        }
    }

    /**
     * #[Route('/path', methods: ['action'])]
     */
    private static class AttributesValuePropertyCompletion extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement psiElement = parameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PsiElement parent = psiElement.getParent();
            if(!(parent instanceof StringLiteralExpression)) {
                return;
            }

            PsiElement attributeNamePsi = PhpPsiUtil.getPrevSibling(parent, psiElement1 -> psiElement1 instanceof PsiWhiteSpace || psiElement1.getNode().getElementType() == PhpTokenTypes.opCOLON);
            if (attributeNamePsi == null || attributeNamePsi.getNode().getElementType() != PhpTokenTypes.IDENTIFIER) {
                return;
            }

            String attributeName = attributeNamePsi.getText();
            if (StringUtils.isBlank(attributeName)) {
                return;
            }

            PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PhpAttribute.class);
            if (phpAttribute == null) {
                return;
            }

            String fqn = phpAttribute.getFQN();
            if (fqn == null) {
                return;
            }

            PhpClass phpClass = PhpElementsUtil.getClassInterface(psiElement.getProject(), fqn);
            if (phpClass == null) {
                return;
            }

            AnnotationCompletionProviderParameter completionParameter = new AnnotationCompletionProviderParameter(parameters, context, result);

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(
                parameters.getOriginalPosition(),
                phpClass,
                attributeName,
                AnnotationPropertyParameter.Type.PROPERTY_VALUE
            );

            for(PhpAnnotationCompletionProvider phpAnnotationExtension : AnnotationUtil.EXTENSION_POINT_COMPLETION.getExtensions()) {
                phpAnnotationExtension.getPropertyValueCompletions(annotationPropertyParameter, completionParameter);
            }
        }
    }

    private void providerWalker(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result, AnnotationPropertyParameter annotationPropertyParameter) {
        AnnotationCompletionProviderParameter completionParameter = new AnnotationCompletionProviderParameter(parameters, context, result);

        for(PhpAnnotationCompletionProvider phpAnnotationExtension : AnnotationUtil.EXTENSION_POINT_COMPLETION.getExtensions()) {
            phpAnnotationExtension.getPropertyValueCompletions(annotationPropertyParameter, completionParameter);
        }
    }

    private class PhpDocAttributeValue extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement psiElement = parameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PsiElement phpDocString = psiElement.getContext();
            if(!(phpDocString instanceof StringLiteralExpression)) {
                return;
            }

            PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(phpDocString, PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER));
            if(propertyName == null) {
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PhpDocTag.class);
            PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
            if(phpClass == null) {
                return;
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(parameters.getOriginalPosition(), phpClass, propertyName.getText(), AnnotationPropertyParameter.Type.PROPERTY_VALUE);
            providerWalker(parameters, context, result, annotationPropertyParameter);

        }
    }

    /**
     * Provides attribute so field properties of annotation
     *
     * "@Foo(<caret>)" => @Foo(name=)
     * "@Foo("foo", <caret>)" => "@Foo("foo", name=)"
     */
    private static class PhpDocAttributeList extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
            PsiElement psiElement = completionParameters.getOriginalPosition();
            if (psiElement == null) {
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag.class);
            if (phpDocTag == null) {
                return;
            }

            PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
            if (phpClass == null) {
                return;
            }

            AnnotationUtil.visitAttributes(phpClass, (attributeName, type, target) -> {
                completionResultSet.addElement(new PhpAnnotationPropertyLookupElement(new AnnotationProperty(attributeName, AnnotationPropertyEnum.fromString(type))));
                return null;
            });

            // extension point for virtual properties
            AnnotationVirtualPropertyCompletionParameter virtualPropertyParameter = null;
            AnnotationCompletionProviderParameter parameter = null;

            for (PhpAnnotationVirtualProperties ep : AnnotationUtil.EP_VIRTUAL_PROPERTIES.getExtensions()) {
                if (virtualPropertyParameter == null) {
                    virtualPropertyParameter = new AnnotationVirtualPropertyCompletionParameter(phpClass);
                }

                if (parameter == null) {
                    parameter = new AnnotationCompletionProviderParameter(completionParameters, processingContext, completionResultSet);
                }

                ep.addCompletions(virtualPropertyParameter, parameter);
            }

            if (virtualPropertyParameter != null) {
                for (Map.Entry<String, AnnotationPropertyEnum> pair : virtualPropertyParameter.getLookupElements().entrySet()) {
                    completionResultSet.addElement(new PhpAnnotationPropertyLookupElement(
                        new AnnotationProperty(pair.getKey(), pair.getValue()))
                    );
                }
            }
        }
    }

    /**
     * Extends attribute completion, but only for alias
     */
    private static class PhpAttributeAlias extends CompletionProvider<CompletionParameters> {
        protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
            PsiElement psiElement = completionParameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PhpAttributesList parentOfType = PsiTreeUtil.getParentOfType(psiElement, PhpAttributesList.class);
            if(parentOfType == null) {
                return;
            }

            attachLookupElements(psiElement.getProject(), parentOfType, completionResultSet);
        }

        private void attachLookupElements(@NotNull Project project, @NotNull PhpAttributesList phpAttributesList, @NotNull CompletionResultSet completionResultSet) {
            Map<String, String> items = new HashMap<>();
            for (UseAliasOption useAliasOption : ApplicationSettings.getUseAliasOptionsWithDefaultFallback()) {
                items.put(useAliasOption.getAlias(), useAliasOption.getClassName());
            }

            items.putAll(getUseAsMap(phpAttributesList));

            for (String fqnClass: FileBasedIndex.getInstance().getAllKeys(PhpAttributesFQNsIndex.KEY, project)) {
                if(!fqnClass.startsWith("\\")) {
                    fqnClass = "\\" + fqnClass;
                }

                // attach class also "@ORM\Entity" if there is not import but an alias via settings
                for (Map.Entry<String, String> aliasFqn : items.entrySet()) {
                    String className = "\\" + StringUtils.stripStart(aliasFqn.getValue(), "\\") + "\\";

                    if (!fqnClass.startsWith(className)) {
                        continue;
                    }

                    String substring = fqnClass.substring(className.length());

                    String lookupString = aliasFqn.getKey() + "\\" + substring;

                    PhpClass underlyingClass = PhpElementsUtil.getClassInterface(project, fqnClass);
                    if (underlyingClass != null) {
                        // check if Attribute is target allowed for context
                        // @see com.jetbrains.php.completion.PhpCompletionContributor.PhpClassRefCompletionProvider.shouldAddElement
                        List<PhpAttribute> rootAttributes = PhpClassCantBeUsedAsAttributeInspection.rootAttributes(underlyingClass).collect(Collectors.toList());
                        if (!rootAttributes.isEmpty() && PhpInapplicableAttributeTargetDeclarationInspection.getInapplicableDeclarationName(phpAttributesList.getParent(), rootAttributes) == null) {
                            PhpClassAnnotationLookupElement phpClassAnnotationLookupElement = new PhpClassAnnotationLookupElement(underlyingClass, new UseAliasOption(aliasFqn.getValue(), aliasFqn.getKey(), true), lookupString);
                            phpClassAnnotationLookupElement.withInsertHandler(AttributeAliasInsertHandler.getInstance());
                            completionResultSet.addElement(underlyingClass.isDeprecated() ? PrioritizedLookupElement.withPriority(phpClassAnnotationLookupElement, -1000) : phpClassAnnotationLookupElement);
                        }
                    }
                }
            }
        }

        private static Map<String, String> getUseAsMap(@NotNull PsiElement phpDocComment) {
            PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(phpDocComment);
            if(scope == null) {
                return Collections.emptyMap();
            }

            Map<String, String> useImports = new HashMap<>();

            for (PhpUseList phpUseList : PhpCodeInsightUtil.collectImports(scope)) {
                for(PhpUse phpUse : phpUseList.getDeclarations()) {
                    String alias = phpUse.getAliasName();
                    if (alias != null) {
                        useImports.put(alias, phpUse.getFQN());
                    }
                }
            }

            return useImports;
        }
    }

    private static class PhpDocBlockTagAnnotations extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
            PsiElement psiElement = completionParameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PhpDocComment parentOfType = PsiTreeUtil.getParentOfType(psiElement, PhpDocComment.class);
            if(parentOfType == null) {
                return;
            }

            AnnotationTarget annotationTarget = PhpElementsUtil.findAnnotationTarget(parentOfType);
            if(annotationTarget == null) {
                return;
            }

            Map<String, String> importMap = AnnotationUtil.getUseImportMap((PsiElement) parentOfType);

            Project project = completionParameters.getPosition().getProject();
            attachLookupElements(project, importMap , annotationTarget, completionResultSet);

        }

        private void attachLookupElements(Project project, Map<String, String> importMap, AnnotationTarget foundTarget, CompletionResultSet completionResultSet) {
            for(PhpAnnotation phpClass: getPhpAnnotationTargetClasses(project, foundTarget)) {
                final PhpClass underlyingClass = phpClass.getPhpClass();

                String fqnClass = underlyingClass.getFQN();
                if(!fqnClass.startsWith("\\")) {
                    fqnClass = "\\" + fqnClass;
                }

                PhpClassAnnotationLookupElement lookupElement = new PhpClassAnnotationLookupElement(underlyingClass)
                    .withInsertHandler(AnnotationTagInsertHandler.getInstance());

                for(Map.Entry<String, String> entry: importMap.entrySet()) {
                    if(fqnClass.startsWith(entry.getValue() + "\\")) {
                        lookupElement.withTypeText(entry.getKey() + fqnClass.substring(entry.getValue().length()));
                    }
                }

                // attach class also "@ORM\Entity" if there is not import but an alias via settings
                for (UseAliasOption useAliasOption : ApplicationSettings.getUseAliasOptionsWithDefaultFallback()) {
                    String className = "\\" + StringUtils.stripStart(useAliasOption.getClassName(), "\\") + "\\";

                    if (!fqnClass.startsWith(className)) {
                        continue;
                    }

                    String substring = fqnClass.substring(className.length());

                    String lookupString = useAliasOption.getAlias() + "\\" + substring;
                    PhpClassAnnotationLookupElement phpClassAnnotationLookupElement = new PhpClassAnnotationLookupElement(underlyingClass, useAliasOption, lookupString);
                    phpClassAnnotationLookupElement.withInsertHandler(AnnotationTagInsertHandler.getInstance());

                    completionResultSet.addElement(underlyingClass.isDeprecated() ? PrioritizedLookupElement.withPriority(phpClassAnnotationLookupElement, -1000) : phpClassAnnotationLookupElement);
                }

                completionResultSet.addElement(underlyingClass.isDeprecated() ? PrioritizedLookupElement.withPriority(lookupElement, -1000) : lookupElement);
            }
        }

        private Collection<PhpAnnotation> getPhpAnnotationTargetClasses(Project project, AnnotationTarget foundTarget) {
            // @TODO: how handle unknown types
            return AnnotationUtil.getAnnotationsOnTargetMap(project,
                foundTarget,
                AnnotationTarget.ALL,
                AnnotationTarget.UNKNOWN,
                AnnotationTarget.UNDEFINED
            ).values();
        }

    }

    private static class PhpDocBlockTagAlias extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
            PsiElement psiElement = completionParameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PsiElement phpDocTag = psiElement.getParent();
            if(!(phpDocTag instanceof PhpDocTag)) {
                return;
            }

            String name = ((PhpDocTag) phpDocTag).getName();
            if(!(name.startsWith("@"))) {
                return;
            }

            int start = name.indexOf("\\");
            if(start == -1) {
                return;
            }

            name = name.substring(1, start);

            Map<String, String> importMap = AnnotationUtil.getUseImportMap(phpDocTag);
            if(!importMap.containsKey(name)) {
                return;
            }

            // find annotation scope, to filter classes
            AnnotationTarget annotationTarget = PhpElementsUtil.findAnnotationTarget(PsiTreeUtil.getParentOfType(psiElement, PhpDocComment.class));
            if(annotationTarget == null) {
                annotationTarget = AnnotationTarget.UNKNOWN;
            }


            // force trailing backslash on namespace
            String namespace = importMap.get(name);
            if(!namespace.startsWith("\\")) {
                namespace = "\\" + namespace;
            }


            Map<String, PhpAnnotation> annotationMap = AnnotationUtil.getAnnotationsOnTargetMap(psiElement.getProject(), AnnotationTarget.ALL, AnnotationTarget.UNDEFINED, AnnotationTarget.UNKNOWN, annotationTarget);

            for(PhpClass phpClass: PhpIndexUtil.getPhpClassInsideNamespace(psiElement.getProject(), namespace)) {
                String fqnName = StringUtils.stripStart(phpClass.getFQN(), "\\");
                if(annotationMap.containsKey(fqnName)) {
                    PhpAnnotation phpAnnotation = annotationMap.get(fqnName);
                    if(phpAnnotation != null && phpAnnotation.matchOneOf(AnnotationTarget.ALL, AnnotationTarget.UNDEFINED, AnnotationTarget.UNKNOWN, annotationTarget)) {
                        String subNamespace = fqnName.substring(namespace.length());
                        String lookupString = name + "\\" + subNamespace;
                        completionResultSet.addElement(LookupElementBuilder.create(lookupString).withTypeText(phpClass.getPresentableFQN(), true).withIcon(phpClass.getIcon()).withInsertHandler(AnnotationTagInsertHandler.getInstance()));
                    }
                }

            }

        }
    }

    /**
     * Completion for const fields inside phpdoc @Route(name=ClassName::<FOO>)
     */
    private static class PhpDocClassConstantCompletion extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement psiElement = parameters.getOriginalPosition();
            if(psiElement == null) {
                return;
            }

            PhpClass phpClass = AnnotationUtil.getClassFromConstant(psiElement);
            if(phpClass != null) {
                phpClass.getFields().stream().filter(Field::isConstant).forEach(field ->
                    result.addElement(LookupElementBuilder.create(field.getName()).withIcon(PhpIcons.FIELD).withTypeText(phpClass.getName(), true))
                );
            }
        }
    }
}
