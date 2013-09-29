package de.espend.idea.php.annotation.reference;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationExtension;
import de.espend.idea.php.annotation.Settings;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.WorkaroundUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class AnnotationPropertyValueReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar) {
        if(WorkaroundUtil.isClassFieldName("com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes", "phpDocAttributeList")) {
            psiReferenceRegistrar.registerReferenceProvider(AnnotationPattern.getDefaultPropertyValueString(), new PropertyValueDefaultReferences());
            psiReferenceRegistrar.registerReferenceProvider(AnnotationPattern.getPropertyValueString(), new PropertyValueReferences());
        }
    }

    private class PropertyValueDefaultReferences extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {

            if(!Settings.getInstance(psiElement.getProject()).pluginEnabled) {
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

            if(!Settings.getInstance(psiElement.getProject()).pluginEnabled) {
                return new PsiReference[0];
            }

            PhpClass phpClass = getValidAnnotationClass(psiElement);
            if(phpClass == null) {
                return new PsiReference[0];
            }

            AnnotationPropertyParameter annotationPropertyParameter = new AnnotationPropertyParameter(psiElement, phpClass, AnnotationPropertyParameter.Type.ARRAY);
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

        for(PhpAnnotationExtension phpAnnotationExtension : AnnotationUtil.getProvider()) {
            Collection<PsiReference> providerReferences = phpAnnotationExtension.getPropertyReferences(annotationPropertyParameter, referencesByElementParameter);
            if(providerReferences != null) {
                psiReferences.addAll(providerReferences);
            }
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }

}
