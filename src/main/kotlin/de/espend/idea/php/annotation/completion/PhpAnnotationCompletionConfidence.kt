package de.espend.idea.php.annotation.completion

import com.intellij.codeInsight.completion.CompletionConfidence
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.util.ThreeState
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocStubElementTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.patterns.PhpPatterns
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.impl.PhpPsiElementImpl

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class PhpAnnotationCompletionConfidence : CompletionConfidence() {
    override fun shouldSkipAutopopup(
        editor: Editor,
        contextElement: PsiElement,
        psiFile: PsiFile,
        offset: Int,
    ): ThreeState {
        if (psiFile !is PhpFile) {
            return ThreeState.UNSURE
        }

        when (val context = contextElement.context) {
            is StringLiteralExpression -> {
                // foo="<|>"
                if (PhpPatterns.psiElement(PhpDocElementTypes.phpDocString).accepts(context)) {
                    return ThreeState.NO
                }
            }

            is PhpDocComment -> {
                // * <|>
                if (
                    PhpPatterns.psiElement().afterLeafSkipping(
                        PhpPatterns.psiElement(PsiWhiteSpace::class.java),
                        PhpPatterns.psiElement(PhpDocTokenTypes.DOC_LEADING_ASTERISK),
                    ).accepts(contextElement)
                ) {
                    return ThreeState.NO
                }
            }

            is PhpPsiElementImpl<*> -> {
                // @Foo(<|>)
                if (PhpPatterns.psiElement(PhpDocElementTypes.phpDocAttributeList).accepts(context)) {
                    return ThreeState.NO
                }

                // @<|>
                if (PhpPatterns.psiElement(PhpDocStubElementTypes.phpDocTag).accepts(context)) {
                    return ThreeState.NO
                }
            }
        }

        return ThreeState.UNSURE
    }
}
