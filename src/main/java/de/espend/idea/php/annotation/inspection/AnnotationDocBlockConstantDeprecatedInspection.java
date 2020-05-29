package de.espend.idea.php.annotation.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.inspection.visitor.PhpDocTagWithUsePsiElementVisitor;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

/**
 * Attach constant deprecated also includes "::class"
 *
 * - "@FOO(test=Test::cl<caret>ass)"
 * - "@FOO(test=Test::VERS<caret>ION)"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocBlockConstantDeprecatedInspection extends LocalInspectionTool {
    public static final String MESSAGE = "[Annotations] Deprecated usage";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpDocTagWithUsePsiElementVisitor(holder, this::visitAnnotationDocTag);
    }

    private void visitAnnotationDocTag(@NotNull PhpDocTag phpDocTag, @NotNull ProblemsHolder holder, @NotNull Function<Void, Map<String, String>> lazyUseImporterCollector) {
        for (PsiElement element : PsiTreeUtil.collectElements(phpDocTag, psiElement -> psiElement.getNode().getElementType() == PhpDocTokenTypes.DOC_STATIC)) {
            PsiElement nextSibling = element.getNextSibling();
            if (nextSibling == null || nextSibling.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER) {
                continue;
            }

            PsiElement prevSibling = element.getPrevSibling();
            if (prevSibling == null) {
                return;
            }

            String namespaceForDocIdentifier = PhpDocUtil.getNamespaceForDocIdentifier(prevSibling);
            if (namespaceForDocIdentifier == null) {
                return;
            }

            String clazz = AnnotationInspectionUtil.getClassFqnString(namespaceForDocIdentifier, lazyUseImporterCollector);
            if (clazz == null) {
                return;
            }

            PhpClass phpClass = PhpElementsUtil.getClassInterface(phpDocTag.getProject(), clazz);
            if (phpClass == null) {
                return;
            }

            // ::class direct class access
            String text = nextSibling.getText();
            if ("class".equals(text)) {
                if (phpClass.isDeprecated()) {
                    holder.registerProblem(
                        nextSibling,
                        MESSAGE,
                        ProblemHighlightType.LIKE_DEPRECATED
                    );
                }

                return;
            }

            // ::CONST fetch the field
            Field fieldByName = phpClass.findFieldByName(text, true);
            if (fieldByName != null && fieldByName.isConstant() && fieldByName.isDeprecated()) {
                holder.registerProblem(
                    nextSibling,
                    MESSAGE,
                    ProblemHighlightType.LIKE_DEPRECATED
                );
            }
        }
    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }
}
