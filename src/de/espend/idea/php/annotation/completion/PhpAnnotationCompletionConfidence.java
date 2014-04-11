package de.espend.idea.php.annotation.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.ThreeState;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpPsiElementImpl;
import org.jetbrains.annotations.NotNull;


public class PhpAnnotationCompletionConfidence extends CompletionConfidence {
    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {

        if(!(psiFile instanceof PhpFile)) {
            return ThreeState.UNSURE;
        }

        PsiElement context = contextElement.getContext();
        if(context instanceof StringLiteralExpression) {

            // foo="<|>"
            if(PhpPatterns.psiElement(PhpDocElementTypes.phpDocString).accepts(context)) {
                return ThreeState.NO;
            }

        }

        if(context instanceof PhpDocComment) {

            // * <|>
            if(PhpPatterns.psiElement().beforeLeafSkipping(PhpPatterns.psiElement(PhpDocTokenTypes.DOC_LEADING_ASTERISK), PhpPatterns.psiElement(PsiWhiteSpace.class)).accepts(context)) {
                return ThreeState.NO;
            }

        }

        if(context instanceof PhpPsiElementImpl) {

            // @Foo(<|>)
            if(PhpPatterns.psiElement(PhpDocElementTypes.phpDocAttributeList).accepts(context)) {
                return ThreeState.NO;
            }

            // @<|>
            if(PhpPatterns.psiElement(PhpDocElementTypes.phpDocTag).accepts(context)) {
                return ThreeState.NO;
            }

        }

        return ThreeState.UNSURE;
    }

}
