package de.espend.idea.php.annotation.util;

import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PhpElementsUtil {

    @Nullable
    static public PhpClass getClass(Project project, String className) {
        Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(className);
        return classes.isEmpty() ? null : classes.iterator().next();
    }

    @Nullable
    static public AnnotationTarget findAnnotationTarget(@Nullable PhpDocComment phpDocComment) {

        if(phpDocComment == null) {
            return null;
        }

        if(phpDocComment.getNextPsiSibling() instanceof Method) {
            return AnnotationTarget.METHOD;
        }

        if(PlatformPatterns.psiElement(PhpElementTypes.CLASS_FIELDS).accepts(phpDocComment.getNextPsiSibling())) {
            return AnnotationTarget.PROPERTY;
        }

        if(phpDocComment.getNextPsiSibling() instanceof PhpClass) {
            return AnnotationTarget.METHOD;
        }

        // workaround: if file has no use statements all is wrapped inside a group
        if(phpDocComment.getNextPsiSibling() instanceof GroupStatement) {
            PsiElement groupStatement =  phpDocComment.getNextPsiSibling();
            if(groupStatement != null && groupStatement.getFirstChild() instanceof PhpClass) {
                return AnnotationTarget.CLASS;
            }
        }

        return null;
    }

}
