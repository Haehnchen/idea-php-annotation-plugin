package de.espend.idea.php.annotation.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AnnotationDeprecatedInspection extends LocalInspectionTool {
    public static final String MESSAGE = "Annotation is deprecated";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof PhpDocTag && AnnotationUtil.isAnnotationPhpDocTag((PhpDocTag) element)) {
                    visitAnnotationDocTag((PhpDocTag) element, holder);
                }

                super.visitElement(element);
            }
        };
    }

    private void visitAnnotationDocTag(PhpDocTag phpDocTag, @NotNull ProblemsHolder holder) {
        PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
        if (phpClass == null) {
            return;
        }

        if (phpClass.isDeprecated()) {
            PsiElement firstChild = phpDocTag.getFirstChild();
            if (firstChild == null || firstChild.getNode().getElementType() != PhpDocElementTypes.DOC_TAG_NAME) {
                return;
            }

            Collection<LocalQuickFix> replacementForDeprecatedAnnotationClass = AnnotationUtil.findQuickFixesForDeprecatedAnnotationClass(phpDocTag, phpClass);
            replacementForDeprecatedAnnotationClass.forEach(localQuickFix -> holder.registerProblem(
                    firstChild,
                    MESSAGE,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    localQuickFix
            ));

            holder.registerProblem(firstChild, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }
}
