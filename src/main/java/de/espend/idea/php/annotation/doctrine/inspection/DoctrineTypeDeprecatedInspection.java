package de.espend.idea.php.annotation.doctrine.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

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
        public void visitElement(PsiElement element) {
            if (!(element instanceof StringLiteralExpression) || element.getNode().getElementType() != PhpDocElementTypes.phpDocString) {
                super.visitElement(element);
                return;
            }

            String contents = ((StringLiteralExpression) element).getContents();
            if (StringUtils.isBlank(contents)) {
                super.visitElement(element);
                return;
            }

            PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(element, PhpDocTag.class);
            if (phpDocTag == null) {
                super.visitElement(element);
                return;
            }

            PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(element, PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER));
            if(propertyName == null) {
                super.visitElement(element);
                return;
            }

            String text = propertyName.getText();
            if ("type".equalsIgnoreCase(text)) {
                PhpDocTagAnnotation phpDocAnnotationContainer = AnnotationUtil.getPhpDocAnnotationContainer(phpDocTag);
                if (phpDocAnnotationContainer != null) {
                    PhpClass tagPhpClass = phpDocAnnotationContainer.getPhpClass();
                    if (PhpLangUtil.equalsClassNames(tagPhpClass.getPresentableFQN(), "Doctrine\\ORM\\Mapping\\Column")) {
                        for (PhpClass columnPhpClass : DoctrineUtil.getColumnTypesTargets(holder.getProject(), contents)) {
                            if (!columnPhpClass.isDeprecated()) {
                                continue;
                            }

                            String deprecationMessage = PhpElementsUtil.getClassDeprecatedMessage(columnPhpClass);

                            holder.registerProblem(
                                element,
                                "[Annotations] " + (deprecationMessage != null ? deprecationMessage : String.format("Field '%s' is deprecated", text)),
                                ProblemHighlightType.LIKE_DEPRECATED
                            );

                            break;
                        }
                    }
                }
            }

            super.visitElement(element);
        }
    }
}
