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
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.util.WorkaroundUtil;

public class AnnotationPattern {
    public static ElementPattern<PsiElement> getDocBlockTag() {
        return
            PlatformPatterns.or(
                PlatformPatterns.psiElement()
                    .withSuperParent(1, PhpDocPsiElement.class)
                         .withParent(PhpDocComment.class)
                .withLanguage(PhpLanguage.INSTANCE)
            ,
                // eap:
                // * @<completion>
                //
                // "@" char is not detected on lexer, so provider additional asterisk check for more secured pattern filter
                PlatformPatterns.psiElement()
                    .afterLeafSkipping(
                        PlatformPatterns.or(
                            PlatformPatterns.psiElement(PsiWhiteSpace.class)
                        ),
                        PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LEADING_ASTERISK)

                    ).withSuperParent(1, PhpDocPsiElement.class)
                .withLanguage(PhpLanguage.INSTANCE)
            );
    }

    /**
     * fire on: @Callback(<completion>), @Callback("", <completion>)
     */
    public static ElementPattern<PsiElement> getDocAttribute() {
        // @TODO: use eap psi
        // @TODO: multiline parser failure: check on eap
        return PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
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
        if(WorkaroundUtil.isClassFieldName("com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes", "phpDocAttributeList")) {
            return PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STRING)
                .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                .afterLeafSkipping(
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText(PlatformPatterns.string().contains("=")),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
                )
                .withParent(PlatformPatterns
                    .psiElement(PhpDocElementTypes.phpDocAttributeList)
                    .withParent(PlatformPatterns
                        .psiElement(PhpDocElementTypes.phpDocTag)
                    )
                ));
        }

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

    /**
     * matches "@Callback("<value>", foo...)"
     * TODO: is this also valid "@Callback(key="", "<value>")"?
     */
    public static ElementPattern<PsiElement> getDefaultPropertyValue() {

        return PlatformPatterns
            .psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).afterLeaf(
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText(PlatformPatterns.string().equalTo("\"")).afterLeaf(
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LPAREN)
                )
            )
            .withParent(PlatformPatterns
                .psiElement(PhpDocElementTypes.phpDocTagValue)
                .withParent(PlatformPatterns
                    .psiElement(PhpDocElementTypes.phpDocTag)
                )
            )
            .withLanguage(PhpLanguage.INSTANCE);
    }

    /**
     * only usable up to phpstorm 7
     */
    public static ElementPattern<StringLiteralExpression> getDefaultPropertyValueString() {

        return PlatformPatterns
             .psiElement(StringLiteralExpression.class).afterLeaf(
                 PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LPAREN)
             )
             .withParent(PlatformPatterns
                 .psiElement(PhpDocElementTypes.phpDocAttributeList)
                 .withParent(PlatformPatterns
                     .psiElement(PhpDocElementTypes.phpDocTag)
                 )
             )
             .withLanguage(PhpLanguage.INSTANCE);
    }

    /**
     * only usable up to phpstorm 7
     */
    public static ElementPattern<StringLiteralExpression> getPropertyValueString() {

        return PlatformPatterns
                .psiElement(StringLiteralExpression.class).afterLeaf(
                        PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("=")
                )
                .withParent(PlatformPatterns
                        .psiElement(PhpDocElementTypes.phpDocAttributeList)
                        .withParent(PlatformPatterns
                                .psiElement(PhpDocElementTypes.phpDocTag)
                        )
                )
                .withLanguage(PhpLanguage.INSTANCE);

    }

}
