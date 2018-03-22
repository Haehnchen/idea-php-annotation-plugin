package de.espend.idea.php.annotation.util;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.Processor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpDocTagAnnotationRecursiveElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {
    @NotNull
    private final Processor<Pair<String, PhpDocTag>> processor;

    public PhpDocTagAnnotationRecursiveElementWalkingVisitor(@NotNull Processor<Pair<String, PhpDocTag>> processor) {
        this.processor = processor;
    }

    @Override
    public void visitElement(PsiElement element) {
        if ((element instanceof PhpDocTag)) {
            visitPhpDocTag((PhpDocTag) element);
        }

        super.visitElement(element);
    }

    private void visitPhpDocTag(@NotNull PhpDocTag phpDocTag) {
        // "@var" and user non related tags dont need an action
        if(AnnotationUtil.NON_ANNOTATION_TAGS.contains(phpDocTag.getName())) {
            return;
        }

        String annotationFqnName = StringUtils.stripStart(getClassNameReference(phpDocTag, AnnotationUtil.getUseImportMap(phpDocTag)), "\\");

        if(annotationFqnName != null && StringUtils.isNotBlank(annotationFqnName)) {
            this.processor.process(Pair.create(annotationFqnName, phpDocTag));
        }
    }

    @Nullable
    private static String getClassNameReference(@NotNull PhpDocTag phpDocTag, @NotNull Map<String, String> useImports) {

        if(useImports.size() == 0) {
            return null;
        }

        String annotationName = phpDocTag.getName();
        if(StringUtils.isBlank(annotationName)) {
            return null;
        }

        if(annotationName.startsWith("@")) {
            annotationName = annotationName.substring(1);
        }

        String className = annotationName;
        String subNamespaceName = "";
        if(className.contains("\\")) {
            className = className.substring(0, className.indexOf("\\"));
            subNamespaceName = annotationName.substring(className.length());
        }

        if(!useImports.containsKey(className)) {
            return null;
        }

        // normalize name
        String annotationFqnName = useImports.get(className) + subNamespaceName;
        if(!annotationFqnName.startsWith("\\")) {
            annotationFqnName = "\\" + annotationFqnName;
        }

        return annotationFqnName;
    }
}