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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
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
                        visitAnnotationDocTag((PhpDocTag) element, holder);
                    }
                    super.visitElement(element);
                }
            });
        }

        return super.buildVisitor(holder, isOnTheFly);
    }

    private void visitAnnotationDocTag(PhpDocTag phpDocTag, @NotNull ProblemsHolder holder) {

        String name = phpDocTag.getName();
        if(StringUtils.isBlank(name) || AnnotationUtil.NON_ANNOTATION_TAGS.contains(name)) {
            return;
        }

        PsiElement firstChild = phpDocTag.getFirstChild();
         /* @TODO: not working  firstChild.getNode().getElementType() == PhpDocElementTypes.DOC_TAG_NAME */
        if(firstChild == null) {
            return;
        }

        PhpClass annotationReference = AnnotationUtil.getAnnotationReference(phpDocTag);
        if(annotationReference != null) {
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
