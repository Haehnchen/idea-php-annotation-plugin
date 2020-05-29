package de.espend.idea.php.annotation.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.inspection.visitor.PhpDocTagWithUsePsiElementVisitor;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

/**
 * Provide inpsection check for the class of "foo=Foo\Foo::cla<caret>ss"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocBlockClassConstantNotFoundInspection extends LocalInspectionTool {
    public static final String MESSAGE = "[Annotations] Class not found";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpDocTagWithUsePsiElementVisitor(holder, this::visitAnnotationDocTag);
    }

    private void visitAnnotationDocTag(@NotNull PhpDocTag phpDocTag, @NotNull ProblemsHolder holder, @NotNull Function<Void, Map<String, String>> lazyUseImporterCollector) {
        for (PsiElement element : PsiTreeUtil.collectElements(phpDocTag, psiElement -> psiElement.getNode().getElementType() == PhpDocTokenTypes.DOC_STATIC)) {
            PsiElement nextSibling = element.getNextSibling();
            if (nextSibling == null || nextSibling.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER || !"class".equals(nextSibling.getText())) {
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

            PhpClass classInterface = PhpElementsUtil.getClassInterface(phpDocTag.getProject(), clazz);
            if (classInterface == null) {
                holder.registerProblem(
                    nextSibling,
                    MESSAGE,
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                );
            }
        }
    }


    @Override
    public boolean runForWholeFile() {
        return true;
    }
}
