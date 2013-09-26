package de.espend.idea.php.annotation.pattern;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocPsiElement;

public class AnnotationPattern {
    public static ElementPattern<PsiElement> getDocBlockTag() {
        return PlatformPatterns
            .psiElement()
                .withSuperParent(1, PhpDocPsiElement.class)
                     .withParent(PhpDocComment.class)
            .withLanguage(PhpLanguage.INSTANCE);
    }

    /**
     * fire on: @Callback(<completion>), @Callback("", <completion>)
     */
    public static ElementPattern<PsiElement> getDocAttribute() {
        // @TODO: use eap psi
        // @TODO: multiline parser failure: check on eap
        return PlatformPatterns.psiElement()
            .afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_COMMA),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LPAREN)
                )

            )
            .inside(PlatformPatterns
                .psiElement(PhpDocElementTypes.phpDocTag)
            )
            .withLanguage(PhpLanguage.INSTANCE);
    }
}
