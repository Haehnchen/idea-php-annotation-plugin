package de.espend.idea.php.annotation.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

public class AnnotationDocTagAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {

        if(!(psiElement instanceof PhpDocTag)) {
            return;
        }

        String name = ((PhpDocTag) psiElement).getName();
        if(AnnotationUtil.NON_ANNOTATION_TAGS.contains(name)) {
            return;
        }

        PhpDocComment phpDocComment = PsiTreeUtil.getParentOfType(psiElement, PhpDocComment.class);
        if(phpDocComment == null) {
            return;
        }

        PsiElement nextPsiElement = phpDocComment.getNextPsiSibling();
        if(nextPsiElement == null || !(nextPsiElement instanceof Method || nextPsiElement instanceof PhpClass || nextPsiElement.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS)) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference(((PhpDocTag) psiElement));
        if(phpClass == null) {

            PhpAnnotationDocTagAnnotatorParameter parameter = new PhpAnnotationDocTagAnnotatorParameter((PhpDocTag) psiElement, holder);
            for(PhpAnnotationDocTagAnnotator annotator: AnnotationUtil.EP_DOC_TAG_ANNOTATOR.getExtensions()) {
                annotator.annotate(parameter);
            }

            return;
        }

        PhpAnnotationDocTagAnnotatorParameter parameter = new PhpAnnotationDocTagAnnotatorParameter(phpClass, (PhpDocTag) psiElement, holder);
        for(PhpAnnotationDocTagAnnotator annotator: AnnotationUtil.EP_DOC_TAG_ANNOTATOR.getExtensions()) {
            annotator.annotate(parameter);
        }


    }



}

