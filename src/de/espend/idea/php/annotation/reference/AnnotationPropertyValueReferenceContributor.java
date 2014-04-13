package de.espend.idea.php.annotation.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationReferencesProvider;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.annotation.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class AnnotationPropertyValueReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(AnnotationPattern.getDefaultPropertyValueString(), new PropertyValueDefaultReferences());
        psiReferenceRegistrar.registerReferenceProvider(AnnotationPattern.getPropertyValueString(), new PropertyValueReferences());
    }

    private class PropertyValueDefaultReferences extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

            if(!PluginUtil.isEnabled(psiElement)) {
                return new PsiReference[0];
            }

            PhpClass phpClass = getValidAnnotationClass(psiElement);
            if(phpClass == null) {
                return new PsiReference[0];
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(psiElement, phpClass, AnnotationPropertyParameter.Type.DEFAULT);
            return addPsiReferences(psiElement, processingContext, annotationPropertyParameter);

        }
    }

    private class PropertyValueReferences extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

            if(!PluginUtil.isEnabled(psiElement)) {
                return new PsiReference[0];
            }

            PhpClass phpClass = getValidAnnotationClass(psiElement);
            if(phpClass == null) {
                return new PsiReference[0];
            }

            PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(psiElement, PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER));
            if(propertyName == null) {
                return new PsiReference[0];
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(psiElement, phpClass, propertyName.getText(), AnnotationPropertyParameter.Type.STRING);
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
        ArrayList<PsiReference> psiReferences = new ArrayList<PsiReference>();

        ReferencesByElementParameter referencesByElementParameter = new ReferencesByElementParameter(psiElement, processingContext);

        for(PhpAnnotationReferencesProvider phpAnnotationExtension : AnnotationUtil.EXTENSION_POINT_REFERENCES.getExtensions()) {
            PsiReference[] references = phpAnnotationExtension.getPropertyReferences(annotationPropertyParameter, referencesByElementParameter);
            if(references != null && references.length > 0) {
                psiReferences.addAll(Arrays.asList(references));
            }
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }

}
