package de.espend.idea.php.annotation.doctrine.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Check for underlay class deprecations of Column type class from Doctrine
 *
 * Example:
 *  - '\Doctrine\ORM\Mapping\Column(type="json_array")'
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineTypeDeprecatedInspection extends LocalInspectionTool {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DoctrineTypePropertyVisitor(holder);
    }

    private static class DoctrineTypePropertyVisitor extends PsiElementVisitor {
        private final ProblemsHolder holder;

        public DoctrineTypePropertyVisitor(ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitElement(@NotNull PsiElement element) {
            String contents = getContentIfTypeValid(element, "\\Doctrine\\ORM\\Mapping\\Column", "type");
            if (contents != null) {
                for (PhpClass columnPhpClass : DoctrineUtil.getColumnTypesTargets(holder.getProject(), contents)) {
                    if (!columnPhpClass.isDeprecated()) {
                        continue;
                    }

                    String deprecationMessage = PhpElementsUtil.getClassDeprecatedMessage(columnPhpClass);

                    holder.registerProblem(
                        element,
                        "[Annotations] " + (deprecationMessage != null ? deprecationMessage : String.format("Field '%s' is deprecated", contents)),
                        ProblemHighlightType.LIKE_DEPRECATED
                    );

                    break;
                }
            }

            super.visitElement(element);
        }
    }

    @Nullable
    private static String getContentIfTypeValid(@NotNull PsiElement psiElement, @NotNull String clazz, @NotNull String property) {
        if (AnnotationPattern.getAttributesValueReferencesPattern().accepts(psiElement)) {
            PsiElement attributeNamePsi = PhpPsiUtil.getPrevSibling(psiElement, psiElement1 -> psiElement1 instanceof PsiWhiteSpace || psiElement1.getNode().getElementType() == PhpTokenTypes.opCOLON);
            if (attributeNamePsi != null && attributeNamePsi.getNode().getElementType() == PhpTokenTypes.IDENTIFIER) {
                String text = attributeNamePsi.getText();
                if (text.equals("type")) {
                    PhpAttribute phpAttribute = PsiTreeUtil.getParentOfType(psiElement, PhpAttribute.class);
                    if (phpAttribute != null) {
                        String fqn = phpAttribute.getFQN();
                        if (fqn != null) {
                            return ((StringLiteralExpression) psiElement).getContents();
                        }
                    }
                }
            }

        } else {
            if (psiElement instanceof StringLiteralExpression && psiElement.getNode().getElementType() == PhpDocElementTypes.phpDocString) {
                PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(psiElement, PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER));
                if(propertyName != null && property.equals(propertyName.getText())) {
                    PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag.class);
                    if (phpDocTag != null) {
                        PhpDocTagAnnotation phpDocAnnotationContainer = AnnotationUtil.getPhpDocAnnotationContainer(phpDocTag);
                        if (phpDocAnnotationContainer != null && PhpLangUtil.equalsClassNames(phpDocAnnotationContainer.getPhpClass().getFQN(), clazz)) {
                            return ((StringLiteralExpression) psiElement).getContents();
                        }
                    }
                }
            }
        }

        return null;
    }
}
