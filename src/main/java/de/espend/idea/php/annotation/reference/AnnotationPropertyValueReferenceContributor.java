package de.espend.idea.php.annotation.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationPropertyValueReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(AnnotationPattern.getDefaultPropertyValueString(), new PropertyValueDefaultReferences());
        psiReferenceRegistrar.registerReferenceProvider(AnnotationPattern.getPropertyValueString(), new PropertyValueReferences());

        // #[Route('/path', name: '<caret>')]
        psiReferenceRegistrar.registerReferenceProvider(AnnotationPattern.getAttributesValueReferencesPattern(), new AttributeValueReferences());

        // #[Route('<caret>')]
        psiReferenceRegistrar.registerReferenceProvider(AnnotationPattern.getAttributesDefaultPattern(), new AttributeDefaultReferences());
    }

    /**
     * '@Template("foo.twig.html")'
     * '@Service("foo")'
     */
    private class PropertyValueDefaultReferences extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
            PhpClass phpClass = getValidAnnotationClass(psiElement);
            if(phpClass == null) {
                return new PsiReference[0];
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(psiElement, phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            return addPsiReferences(psiElement, processingContext, annotationPropertyParameter);

        }
    }

    /**
     * '@Template(name="foo.twig.html")'
     */
    private class PropertyValueReferences extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
            PhpClass phpClass = getValidAnnotationClass(psiElement);
            if(phpClass == null) {
                return new PsiReference[0];
            }

            PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(psiElement, PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER));
            if(propertyName == null) {
                return new PsiReference[0];
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(psiElement, phpClass, propertyName.getText(), AnnotationPropertyParameter.Type.PROPERTY_VALUE);
            return addPsiReferences(psiElement, processingContext, annotationPropertyParameter);

        }
    }

    /**
     * #[Route('/path', name: '<caret>')]
     */
    private class AttributeValueReferences extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
            if(!(psiElement instanceof StringLiteralExpression)) {
                return new PsiReference[0];
            }

            PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(psiElement, PhpAttribute.class);
            if (phpAttribute == null) {
                return new PsiReference[0];
            }

            String fqn = phpAttribute.getFQN();
            if (fqn == null) {
                return new PsiReference[0];
            }

            PhpClass phpClass = PhpElementsUtil.getClassInterface(psiElement.getProject(), fqn);
            if (phpClass == null) {
                return new PsiReference[0];
            }

            PsiElement attributeNamePsi = PhpPsiUtil.getPrevSibling(psiElement, psiElement1 -> psiElement1 instanceof PsiWhiteSpace || psiElement1.getNode().getElementType() == PhpTokenTypes.opCOLON);
            if (attributeNamePsi == null || attributeNamePsi.getNode().getElementType() != PhpTokenTypes.IDENTIFIER) {
                return new PsiReference[0];
            }

            String attributeName = attributeNamePsi.getText();
            if (StringUtils.isBlank(attributeName)) {
                return new PsiReference[0];
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(psiElement, phpClass, attributeName, AnnotationPropertyParameter.Type.PROPERTY_VALUE);
            return addPsiReferences(psiElement, processingContext, annotationPropertyParameter);
        }
    }


    /**
     * #[Route('/path', name: '<caret>')]
     */
    private class AttributeDefaultReferences extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
            if(!(psiElement instanceof StringLiteralExpression)) {
                return new PsiReference[0];
            }

            PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(psiElement, PhpAttribute.class);
            if (phpAttribute == null) {
                return new PsiReference[0];
            }

            String fqn = phpAttribute.getFQN();
            if (fqn == null) {
                return new PsiReference[0];
            }

            PhpClass phpClass = PhpElementsUtil.getClassInterface(psiElement.getProject(), fqn);
            if (phpClass == null) {
                return new PsiReference[0];
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(psiElement, phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            return addPsiReferences(psiElement, processingContext, annotationPropertyParameter);
        }
    }

    @Nullable
    private PhpClass getValidAnnotationClass(PsiElement psiElement) {
        PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag.class);
        if(phpDocTag == null) {
            return null;
        }

        return AnnotationUtil.getAnnotationReference(phpDocTag);
    }

    private PsiReference[] addPsiReferences(PsiElement psiElement, ProcessingContext processingContext, AnnotationPropertyParameter annotationPropertyParameter) {
        ArrayList<PsiReference> psiReferences = new ArrayList<>();

        PhpAnnotationReferenceProviderParameter referencesByElementParameter = new PhpAnnotationReferenceProviderParameter(psiElement, processingContext);

        for(PhpAnnotationReferenceProvider phpAnnotationExtension : AnnotationUtil.EXTENSION_POINT_REFERENCES.getExtensions()) {
            PsiReference[] references = phpAnnotationExtension.getPropertyReferences(annotationPropertyParameter, referencesByElementParameter);
            if(references != null && references.length > 0) {
                psiReferences.addAll(Arrays.asList(references));
            }
        }

        return psiReferences.toArray(new PsiReference[0]);
    }

}
