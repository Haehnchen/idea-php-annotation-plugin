package de.espend.idea.php.annotation.completion;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import de.espend.idea.php.annotation.Settings;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.dict.PhpAnnotation;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AnnotationGoToDeclarationHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {

        if(!Settings.getInstance(psiElement.getProject()).pluginEnabled) {
            return null;
        }

        List<PsiElement> psiElements = new ArrayList<PsiElement>();
        if(AnnotationPattern.getDocAttribute().accepts(psiElement)) {
            this.addPropertyGoto(psiElement, psiElements);
        }

        if (PlatformPatterns.psiElement(PhpDocElementTypes.DOC_TAG_NAME).withText(StandardPatterns.string().startsWith("@")).withLanguage(PhpLanguage.INSTANCE).accepts(psiElement)) {
            this.addDocTagNameGoto(psiElement, psiElements);
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    /**
     * TODO: should be removed if psi.reference is working in eap
     */
    private void addDocTagNameGoto(PsiElement psiElement, List<PsiElement> psiElements) {

        PsiElement phpDocTagValue = psiElement.getContext();
        if(!(phpDocTagValue instanceof PhpDocTag)) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference((PhpDocTag) phpDocTagValue);
        if(phpClass == null) {
            return;
        }

        psiElements.add(phpClass);
    }

    private void addPropertyGoto(PsiElement psiElement, List<PsiElement> psiElements) {

        PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag.class);
        if(phpDocTag == null) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
        if(phpClass == null) {
            return;
        }

        for(Field field: phpClass.getFields()) {
            if(field.getName().equals(psiElement.getText())) {
                psiElements.add(field);
                return;
            }
        }
    }

    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

}
