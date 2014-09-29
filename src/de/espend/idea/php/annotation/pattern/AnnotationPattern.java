package de.espend.idea.php.annotation.pattern;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;

public class AnnotationPattern {


    public static ElementPattern<PsiElement> getDocBlockTagAfterBackslash() {
        return PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TAG_NAME);
    }

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
     * * @ORM\Column(
     *      <completion>,
     * )
     */
    public static ElementPattern<PsiElement> getDocAttribute() {
        return PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
            .afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_COMMA),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LPAREN),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LEADING_ASTERISK)
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
        return PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STRING)
            .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText(PlatformPatterns.string().containsChars("=")),
                        PlatformPatterns.psiElement(PsiWhiteSpace.class)
                    ),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
                )
                .withParent(PlatformPatterns
                    .psiElement(PhpDocElementTypes.phpDocAttributeList)
                    .withParent(PlatformPatterns
                        .psiElement(PhpDocElementTypes.phpDocTag)
                    )
                )
            );
    }

    /**
     * matches "@Callback(<property>=)"
     */
    public static ElementPattern<PsiElement> getPropertyIdentifier(String propertyName) {
        return PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).withText(propertyName)
            .beforeLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("=")
            )
            .withParent(PlatformPatterns.psiElement(PhpDocElementTypes.phpDocAttributeList));
    }

    /**
     * matches "@Callback(propertyName="<value>")"
     */
    public static PsiElementPattern.Capture<StringLiteralExpression> getPropertyIdentifierValue(String propertyName) {
        return PlatformPatterns.psiElement(StringLiteralExpression.class)
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("=")
                ),
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).withText(propertyName)
            )
            .withParent(PlatformPatterns.psiElement(PhpDocElementTypes.phpDocAttributeList));
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

    public static ElementPattern<PsiElement> getClassConstant() {
        return PlatformPatterns.psiElement().afterLeaf(PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STATIC)).withLanguage(PhpLanguage.INSTANCE);
    }

}
