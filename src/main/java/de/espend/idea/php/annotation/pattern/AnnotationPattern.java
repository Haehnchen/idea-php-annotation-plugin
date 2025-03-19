package de.espend.idea.php.annotation.pattern;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocStubElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocPsiElement;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
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
                // all "@<caret>"
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TAG_NAME)
                    .withLanguage(PhpLanguage.INSTANCE)
            );
    }

    public static ElementPattern<PsiElement> getAttributeNamePattern() {
        return PlatformPatterns.psiElement(PhpTokenTypes.IDENTIFIER)
            .withParent(
                PlatformPatterns.psiElement(ClassReference.class).withParent(PhpAttribute.class)
            );
    }

    /**
     * fire on: @Callback(<completion>), @Callback("", <completion>)
     * * @ORM\Column(
     *      <completion>,
     * )
     *
     * On nested docs WHITESPACE is DOC_TEXT
     */
    public static ElementPattern<PsiElement> getDocAttribute() {
        return PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).with(new PatternCondition<>("Whitespace fix") {
                        @Override
                        public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext processingContext) {
                            // nested issue
                            return StringUtils.isBlank(psiElement.getText());
                        }
                    })
                ),
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_COMMA),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LPAREN),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LEADING_ASTERISK)
                )

            )
            .inside(PlatformPatterns
                .psiElement(PhpDocStubElementTypes.phpDocTag)
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
                        .psiElement(PhpDocStubElementTypes.phpDocTag)
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
     */
    public static PsiElementPattern.Capture<PsiElement> getDefaultPropertyValue() {
        return PlatformPatterns
            .psiElement(PhpDocTokenTypes.DOC_STRING)
            .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(PlatformPatterns
                .psiElement(PhpDocElementTypes.phpDocAttributeList)
                .withParent(PlatformPatterns
                    .psiElement(PhpDocStubElementTypes.phpDocTag)
                )
            ))
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
                     .psiElement(PhpDocStubElementTypes.phpDocTag)
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
                                .psiElement(PhpDocStubElementTypes.phpDocTag)
                        )
                )
                .withLanguage(PhpLanguage.INSTANCE);

    }

    /**
     * Pattern for @Foo(Foo::<caret>), @Foo(name=Foo::<caret>)
     */
    public static ElementPattern<PsiElement> getClassConstant() {
        return PlatformPatterns.psiElement().afterLeaf(getDocStaticPattern()).withLanguage(PhpLanguage.INSTANCE);
    }

    /**
     * Pattern @Foo(Foo::<caret>), @Foo(name=Foo::<caret>)
     */
    @NotNull
    public static ElementPattern<PsiElement> getDocStaticPattern() {
        return PlatformPatterns.or(
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STATIC),
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("::") // array lexer workaround having text element in array; WI-32801
        );
    }

    /**
     * Route("/", methods={"<caret>", "POST"})
     * Route("/", methods={"GET", "<caret>"})
     */
    public static ElementPattern<PsiElement> getPropertyArrayPattern() {

        // "methods={"
        PsiElementPattern.Capture<PsiElement> propertyPattern = PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LBRACE)
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class)
                ),
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("=").afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
            )
        );

        return PlatformPatterns.or(
            // methods={"<caret>", "POST"}
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STRING)
                .withParent(StringLiteralExpression.class)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(PsiWhiteSpace.class),
                        PlatformPatterns.psiElement().with(new MyWhiteSpaceAsTextPatternCondition())
                    ),
                    propertyPattern
                ),
            // methods={"POST" , "<caret>"}
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STRING)
                .withParent(StringLiteralExpression.class)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(PsiWhiteSpace.class),
                        PlatformPatterns.psiElement().with(new MyWhiteSpaceAsTextPatternCondition())
                    ),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_COMMA).afterLeafSkipping(
                            PlatformPatterns.or(
                                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                                PlatformPatterns.psiElement().with(new MyWhiteSpaceAsTextPatternCondition()),
                                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STRING),
                                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_COMMA)
                            ),
                        propertyPattern
                    )
                )
            );
    }

    /**
     * #[Route('/path', name: 'action', methods: ['test'])]
     */
    public static ElementPattern<PsiElement> getAttributesArrayPattern() {
        return PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                .withParent(PlatformPatterns.psiElement(PhpPsiElement.class)
                    .withParent(PlatformPatterns.psiElement(ArrayCreationExpression.class).afterLeafSkipping(
                        PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.opCOLON)
                            .afterLeafSkipping(PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER))
                    )
            )));
    }

    /**
     * #[Route('/path', name: '<caret>')]
     */
    public static PsiElementPattern.Capture<StringLiteralExpression> getAttributesValueReferencesPattern() {
        return PlatformPatterns.psiElement(StringLiteralExpression.class)
            .afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.opCOLON)
                    .afterLeafSkipping(PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER))
            ).withParent(PlatformPatterns.psiElement(ParameterList.class).withParent(PhpAttribute.class));
    }

    /**
     * #[Route('/path', name: '<caret>')]
     * @return
     */
    public static PsiElementPattern.@NotNull Capture<PsiElement> getAttributesValuePattern() {
        return PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
            .afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.opCOLON)
                    .afterLeafSkipping(PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER))
            ).withParent(PlatformPatterns.psiElement(ParameterList.class).withParent(PhpAttribute.class)));
    }

    /**
     * #[Route('/path', name: '<caret>')]
     */
    public static PsiElementPattern.Capture<StringLiteralExpression> getAttributesDefaultPattern() {
        return PlatformPatterns.psiElement(StringLiteralExpression.class).with(new PatternCondition<>("default attribute value") {
            @Override
            public boolean accepts(@NotNull StringLiteralExpression stringLiteralExpression, ProcessingContext processingContext) {
                return stringLiteralExpression.getPrevSibling() == null;
            }
        }).withParent(PlatformPatterns.psiElement(ParameterList.class).withParent(PhpAttribute.class));
    }
    /**
     * Get property of enum array eg "methods"
     *
     * Route("/", methods={"<caret>", "POST"})
     * Route("/", methods={"GET", "<caret>"})
     */
    public static ElementPattern<PsiElement> getPropertyNameOfArrayValuePattern() {
        return PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).beforeLeafSkipping(
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                PlatformPatterns.psiElement().with(new MyWhiteSpaceAsTextPatternCondition())
            ),
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("=").beforeLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class),
                    PlatformPatterns.psiElement().with(new MyWhiteSpaceAsTextPatternCondition())
                ),
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LBRACE)
            )
        );
    }

    /**
     * On array whitespace element is DOC_TEXT element
     *
     * Route("/", methods={ "GET", "<caret>"})
     */
    private static class MyWhiteSpaceAsTextPatternCondition extends PatternCondition<PsiElement> {
        public MyWhiteSpaceAsTextPatternCondition() {
            super("Whitespace as DOC_TEXT fix");
        }

        @Override
        public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext processingContext) {
            return psiElement.getNode().getElementType() == PhpDocTokenTypes.DOC_TEXT && StringUtils.isBlank(psiElement.getText());
        }
    }
}
