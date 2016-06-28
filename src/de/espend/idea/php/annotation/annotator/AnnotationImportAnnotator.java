package de.espend.idea.php.annotation.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationImportAnnotator implements PhpAnnotationDocTagAnnotator {
    @Override
    public void annotate(PhpAnnotationDocTagAnnotatorParameter parameter) {

        if(parameter.getAnnotationClass() != null) {
            return;
        }

        PhpDocTag phpDocTag = parameter.getPhpDocTag();
        Collection<PhpClass> phpClasses = AnnotationUtil.getPossibleImportClasses(phpDocTag);
        if(phpClasses.size() == 0) {
            return;
        }

        String tagName = phpDocTag.getName();
        if(StringUtils.isBlank(tagName)) {
            return;
        }

        PsiElement firstChild = phpDocTag.getFirstChild();
         /* @TODO: not working  firstChild.getNode().getElementType() == PhpDocElementTypes.DOC_TAG_NAME */
        if(firstChild == null) {
            return;
        }

        Annotation annotationHolder = parameter.getHolder().createWarningAnnotation(firstChild, "Import class");

        // clean warning; we dont want tooltip, but popover menu entry; @TODO: direct call possible?
        annotationHolder.setHighlightType(ProblemHighlightType.INFORMATION);
        annotationHolder.setTooltip(null);

        for(PhpClass phpClass: phpClasses) {
            annotationHolder.registerFix(new CreatePropertyQuickFix(phpDocTag, "\\" + phpClass.getPresentableFQN()));
        }

    }

    private static class CreatePropertyQuickFix extends BaseIntentionAction {

        final private String className;
        final private PhpDocTag phpDocTag;

        public CreatePropertyQuickFix(PhpDocTag phpDocTag, String className) {
            this.className = className;
            this.phpDocTag = phpDocTag;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "Annotation";
        }

        @NotNull
        @Override
        public String getText() {
            return "Import: " + this.className;
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
            return true;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {

            CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(() -> {

                PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(phpDocTag);
                if(scopeForUseOperator != null) {
                    PhpAliasImporter.insertUseStatement(className, scopeForUseOperator);
                }

            }), getText(), null);

        }

    }
}
