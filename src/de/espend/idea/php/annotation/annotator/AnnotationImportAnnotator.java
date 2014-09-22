package de.espend.idea.php.annotation.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnnotationImportAnnotator implements PhpAnnotationDocTagAnnotator {
    @Override
    public void annotate(PhpAnnotationDocTagAnnotatorParameter parameter) {

        if(parameter.getAnnotationClass() != null) {
            return;
        }

        PhpDocTag phpDocTag = parameter.getPhpDocTag();
        String tagName = phpDocTag.getName();

        String className = tagName;
        if(className.startsWith("@")) {
            className = className.substring(1);
        }

        List<PhpClass> phpClasses = new ArrayList<PhpClass>();

        for(PhpClass annotationClass: AnnotationUtil.getAnnotationsClasses(parameter.getProject())) {
            if(annotationClass.getName().equals(className)) {
                phpClasses.add(annotationClass);
            }
        }

        if(phpClasses.size() == 0) {
            return;
        }

        //Annotation annotationHolder = parameter.getHolder().createWarningAnnotation(new TextRange(phpDocTag.getTextOffset(), phpDocTag.getTextOffset() + tagName.length()), "Import");
        Annotation annotationHolder = parameter.getHolder().createWarningAnnotation(new TextRange(phpDocTag.getTextOffset(), phpDocTag.getTextOffset() + tagName.length()), "Import");
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

            CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {

                            PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(phpDocTag);
                            if(scopeForUseOperator != null) {
                                PhpCodeEditUtil.insertUseStatement(className, scopeForUseOperator);
                            }

                        }
                    });
                }
            }, getText(), null);

        }

    }
}
