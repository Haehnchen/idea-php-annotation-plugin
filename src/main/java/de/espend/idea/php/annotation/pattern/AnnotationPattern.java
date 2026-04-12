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

    private static final ElementPattern<PsiElement> DOC_BLOCK_TAG_AFTER_BACKSLASH =
        PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TAG_NAME);

    private static final ElementPattern<PsiElement> DOC_BLOCK_TAG =
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

    private static final ElementPattern<PsiElement> ATTRIBUTE_NAME_PATTERN =
        PlatformPatterns.psiElement(PhpTokenTypes.IDENTIFIER)
            .withParent(
                PlatformPatterns.psiElement(ClassReference.class).withParent(PhpAttribute.class)
            );

    private static final ElementPattern<PsiElement> DOC_STATIC_PATTERN =
        PlatformPatterns.or(
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STATIC),
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("::") // array lexer workaround having text element in array; WI-32801
        );

    private static final ElementPattern<PsiElement> CLASS_CONSTANT =
        PlatformPatterns.psiElement().afterLeaf(DOC_STATIC_PATTERN).withLanguage(PhpLanguage.INSTANCE);

    /**
     * fire on: @Callback(<completion>), @Callback("", <completion>)
     * * @ORM\Column(
     *      <completion>,
     * )
     *
     * On nested docs WHITESPACE is DOC_TEXT
     */
    private static final ElementPattern<PsiElement> DOC_ATTRIBUTE =
        PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
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

    private static final ElementPattern<PsiElement> POSSIBLE_DOC_TAG =
        PlatformPatterns.psiElement()
            .withSuperParent(1, PhpDocPsiElement.class)
            .withParent(PhpDocComment.class)
            .withLanguage(PhpLanguage.INSTANCE);

    /**
     * matches "@Callback(property="<value>")"
     */
    private static final ElementPattern<PsiElement> TEXT_IDENTIFIER =
        PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STRING)
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

    /**
     * matches "@Callback("<value>", foo...)"
     */
    private static final PsiElementPattern.Capture<PsiElement> DEFAULT_PROPERTY_VALUE =
        PlatformPatterns
            .psiElement(PhpDocTokenTypes.DOC_STRING)
            .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(PlatformPatterns
                .psiElement(PhpDocElementTypes.phpDocAttributeList)
                .withParent(PlatformPatterns
                    .psiElement(PhpDocStubElementTypes.phpDocTag)
                )
            ))
            .withLanguage(PhpLanguage.INSTANCE);

    /**
     * only usable up to phpstorm 7
     */
    private static final ElementPattern<StringLiteralExpression> DEFAULT_PROPERTY_VALUE_STRING =
        PlatformPatterns
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

    /**
     * only usable up to phpstorm 7
     */
    private static final ElementPattern<StringLiteralExpression> PROPERTY_VALUE_STRING =
        PlatformPatterns
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

    private static final PatternCondition<PsiElement> WHITE_SPACE_AS_TEXT_CONDITION = new PatternCondition<PsiElement>("Whitespace as DOC_TEXT fix") {
        @Override
        public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext processingContext) {
            return psiElement.getNode().getElementType() == PhpDocTokenTypes.DOC_TEXT && StringUtils.isBlank(psiElement.getText());
        }
    };

    /**
     * Route("/", methods={"<caret>", "POST"})
     * Route("/", methods={"GET", "<caret>"})
     */
    private static final ElementPattern<PsiElement> PROPERTY_ARRAY_PATTERN = buildPropertyArrayPattern();

    /**
     * #[Route('/path', name: 'action', methods: ['test'])]
     */
    private static final ElementPattern<PsiElement> ATTRIBUTES_ARRAY_PATTERN =
        PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                .withParent(PlatformPatterns.psiElement(PhpPsiElement.class)
                    .withParent(PlatformPatterns.psiElement(ArrayCreationExpression.class).afterLeafSkipping(
                        PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.opCOLON)
                            .afterLeafSkipping(PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER))
                    )
            )));

    /**
     * #[Route('/path', name: '<caret>')]
     */
    private static final PsiElementPattern.Capture<StringLiteralExpression> ATTRIBUTES_VALUE_REFERENCES_PATTERN =
        PlatformPatterns.psiElement(StringLiteralExpression.class)
            .afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.opCOLON)
                    .afterLeafSkipping(PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER))
            ).withParent(PlatformPatterns.psiElement(ParameterList.class).withParent(PhpAttribute.class));

    /**
     * #[Route('/path', name: '<caret>')]
     * @return
     */
    private static final PsiElementPattern.Capture<PsiElement> ATTRIBUTES_VALUE_PATTERN =
        PlatformPatterns.psiElement().withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
            .afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.opCOLON)
                    .afterLeafSkipping(PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER))
            ).withParent(PlatformPatterns.psiElement(ParameterList.class).withParent(PhpAttribute.class)));

    /**
     * #[Route('/path', name: '<caret>')]
     */
    private static final PsiElementPattern.Capture<StringLiteralExpression> ATTRIBUTES_DEFAULT_PATTERN =
        PlatformPatterns.psiElement(StringLiteralExpression.class).with(new PatternCondition<>("default attribute value") {
            @Override
            public boolean accepts(@NotNull StringLiteralExpression stringLiteralExpression, ProcessingContext processingContext) {
                return stringLiteralExpression.getPrevSibling() == null;
            }
        }).withParent(PlatformPatterns.psiElement(ParameterList.class).withParent(PhpAttribute.class));

    /**
     * Get property of enum array eg "methods"
     *
     * Route("/", methods={"<caret>", "POST"})
     * Route("/", methods={"GET", "<caret>"})
     */
    private static final ElementPattern<PsiElement> PROPERTY_NAME_OF_ARRAY_VALUE_PATTERN =
        PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).beforeLeafSkipping(
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                PlatformPatterns.psiElement().with(WHITE_SPACE_AS_TEXT_CONDITION)
            ),
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("=").beforeLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class),
                    PlatformPatterns.psiElement().with(WHITE_SPACE_AS_TEXT_CONDITION)
                ),
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_LBRACE)
            )
        );

    private static ElementPattern<PsiElement> buildPropertyArrayPattern() {
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
                        PlatformPatterns.psiElement().with(WHITE_SPACE_AS_TEXT_CONDITION)
                    ),
                    propertyPattern
                ),
            // methods={"POST" , "<caret>"}
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STRING)
                .withParent(StringLiteralExpression.class)
                .afterLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(PsiWhiteSpace.class),
                        PlatformPatterns.psiElement().with(WHITE_SPACE_AS_TEXT_CONDITION)
                    ),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_COMMA).afterLeafSkipping(
                            PlatformPatterns.or(
                                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                                PlatformPatterns.psiElement().with(WHITE_SPACE_AS_TEXT_CONDITION),
                                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STRING),
                                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_COMMA)
                            ),
                        propertyPattern
                    )
                )
            );
    }

    public static ElementPattern<PsiElement> getDocBlockTagAfterBackslash() {
        return DOC_BLOCK_TAG_AFTER_BACKSLASH;
    }

    public static ElementPattern<PsiElement> getDocBlockTag() {
        return DOC_BLOCK_TAG;
    }

    public static ElementPattern<PsiElement> getAttributeNamePattern() {
        return ATTRIBUTE_NAME_PATTERN;
    }

    public static ElementPattern<PsiElement> getDocAttribute() {
        return DOC_ATTRIBUTE;
    }

    public static ElementPattern<PsiElement> getPossibleDocTag() {
        return POSSIBLE_DOC_TAG;
    }

    /**
     * matches "@Callback(property="<value>")"
     */
    public static ElementPattern<PsiElement> getTextIdentifier() {
        return TEXT_IDENTIFIER;
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
        return DEFAULT_PROPERTY_VALUE;
    }

    /**
     * only usable up to phpstorm 7
     */
    public static ElementPattern<StringLiteralExpression> getDefaultPropertyValueString() {
        return DEFAULT_PROPERTY_VALUE_STRING;
    }

    /**
     * only usable up to phpstorm 7
     */
    public static ElementPattern<StringLiteralExpression> getPropertyValueString() {
        return PROPERTY_VALUE_STRING;
    }

    /**
     * Pattern for @Foo(Foo::<caret>), @Foo(name=Foo::<caret>)
     */
    public static ElementPattern<PsiElement> getClassConstant() {
        return CLASS_CONSTANT;
    }

    /**
     * Pattern @Foo(Foo::<caret>), @Foo(name=Foo::<caret>)
     */
    @NotNull
    public static ElementPattern<PsiElement> getDocStaticPattern() {
        return DOC_STATIC_PATTERN;
    }

    /**
     * Route("/", methods={"<caret>", "POST"})
     * Route("/", methods={"GET", "<caret>"})
     */
    public static ElementPattern<PsiElement> getPropertyArrayPattern() {
        return PROPERTY_ARRAY_PATTERN;
    }

    /**
     * #[Route('/path', name: 'action', methods: ['test'])]
     */
    public static ElementPattern<PsiElement> getAttributesArrayPattern() {
        return ATTRIBUTES_ARRAY_PATTERN;
    }

    /**
     * #[Route('/path', name: '<caret>')]
     */
    public static PsiElementPattern.Capture<StringLiteralExpression> getAttributesValueReferencesPattern() {
        return ATTRIBUTES_VALUE_REFERENCES_PATTERN;
    }

    /**
     * #[Route('/path', name: '<caret>')]
     * @return
     */
    public static PsiElementPattern.@NotNull Capture<PsiElement> getAttributesValuePattern() {
        return ATTRIBUTES_VALUE_PATTERN;
    }

    /**
     * #[Route('/path', name: '<caret>')]
     */
    public static PsiElementPattern.Capture<StringLiteralExpression> getAttributesDefaultPattern() {
        return ATTRIBUTES_DEFAULT_PATTERN;
    }

    /**
     * Get property of enum array eg "methods"
     *
     * Route("/", methods={"<caret>", "POST"})
     * Route("/", methods={"GET", "<caret>"})
     */
    public static ElementPattern<PsiElement> getPropertyNameOfArrayValuePattern() {
        return PROPERTY_NAME_OF_ARRAY_VALUE_PATTERN;
    }
}
