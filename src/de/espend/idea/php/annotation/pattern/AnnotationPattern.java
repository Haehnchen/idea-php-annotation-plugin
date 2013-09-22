package de.espend.idea.php.annotation.pattern;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
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

    public static ElementPattern<PsiElement> getInsideDocAttributeList() {
        // @TODO: use eap psi
        return PlatformPatterns
            .psiElement()
            .inside(PlatformPatterns
                .psiElement(PhpDocElementTypes.phpDocTag)
            )
            .withLanguage(PhpLanguage.INSTANCE);
    }
}
