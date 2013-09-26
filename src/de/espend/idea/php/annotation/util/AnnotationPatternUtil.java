package de.espend.idea.php.annotation.util;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocPsiElement;

public class AnnotationPatternUtil {
    public static ElementPattern<PsiElement> getPossibleDocTag() {
        return PlatformPatterns.psiElement()
            .withSuperParent(1, PhpDocPsiElement.class)
            .withParent(PhpDocComment.class)
            .withLanguage(PhpLanguage.INSTANCE);
    }

    /**
     * matches "@Callback(property="<value>")"
     */
    public static ElementPattern<PsiElement> getTextIdentifier() {

        // @TODO: filter more on EAP
        return PlatformPatterns
            .psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText(PlatformPatterns.string().contains("=\""))
                ),
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
            )
            .withParent(PlatformPatterns
                .psiElement(PhpDocElementTypes.phpDocTagValue)
                .withParent(PlatformPatterns
                    .psiElement(PhpDocElementTypes.phpDocTag)
                )
            )
            .withLanguage(PhpLanguage.INSTANCE);
    }
}
