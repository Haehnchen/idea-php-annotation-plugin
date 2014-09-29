package de.espend.idea.php.annotation.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AnnotationMissingUseInspection extends LocalInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {

        PsiFile psiFile = holder.getFile();

        if(psiFile instanceof PhpFile) {
            psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if(element instanceof PhpDocTag && AnnotationUtil.isAnnotationPhpDocTag((PhpDocTag) element)) {
                        foo((PhpDocTag) element, holder);
                    }
                    super.visitElement(element);
                }
            });
        }

        return super.buildVisitor(holder, isOnTheFly);
    }

    private void foo(PhpDocTag phpDocTag, @NotNull ProblemsHolder holder) {

        PsiElement firstChild = phpDocTag.getFirstChild();
         /* @TODO: not working  firstChild.getNode().getElementType() == PhpDocElementTypes.DOC_TAG_NAME */
        if(firstChild == null) {
            return;
        }

        Collection<PhpClass> phpClasses = AnnotationUtil.getPossibleImportClasses(phpDocTag);
        if(phpClasses.size() == 0) {
            return;
        }

        holder.registerProblem(firstChild, "Missing import", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }

}
