package de.espend.idea.php.annotation.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Inspection DocTags and their imports
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationMissingUseInspection extends LocalInspectionTool {

    public static final String MESSAGE = "Missing import";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(element instanceof PhpDocTag && AnnotationUtil.isAnnotationPhpDocTag((PhpDocTag) element)) {
                    visitAnnotationDocTag((PhpDocTag) element, holder);
                }

                super.visitElement(element);
            }
        };
    }

    private void visitAnnotationDocTag(PhpDocTag phpDocTag, @NotNull ProblemsHolder holder) {

        String name = phpDocTag.getName();
        if(StringUtils.isBlank(name) || AnnotationUtil.NON_ANNOTATION_TAGS.contains(name)) {
            return;
        }

        // Target for our inspection is DocTag name: @Foobar() => Foobar
        // This prevent highlighting the complete DocTag
        PsiElement firstChild = phpDocTag.getFirstChild();
        if(firstChild == null || firstChild.getNode().getElementType() != PhpDocElementTypes.DOC_TAG_NAME) {
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

        Set<String> collect = phpClasses.stream()
            .map(PhpNamedElement::getFQN)
            .collect(Collectors.toSet());

        holder.registerProblem(
            firstChild,
            MESSAGE,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            new ImportUseForAnnotationQuickFix(phpDocTag, collect)
        );
    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }
}
