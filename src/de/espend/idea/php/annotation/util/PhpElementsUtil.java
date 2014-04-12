package de.espend.idea.php.annotation.util;

import com.intellij.openapi.project.Project;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import org.jetbrains.annotations.NotNull;
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

    @Nullable
    public static <T extends PsiElement> T getPrevSiblingOfPatternMatch(@Nullable PsiElement sibling, ElementPattern<T> pattern) {
        if (sibling == null) return null;
        for (PsiElement child = sibling.getPrevSibling(); child != null; child = child.getPrevSibling()) {
            if (pattern.accepts(child)) {
                //noinspection unchecked
                return (T)child;
            }
        }
        return null;
    }

    @Nullable
    static public PhpClass getClassInterface(Project project, @NotNull String className) {

        // api workaround for at least interfaces
        if(!className.startsWith("\\")) {
            className = "\\" + className;
        }

        Collection<PhpClass> phpClasses = PhpIndex.getInstance(project).getAnyByFQN(className);
        return phpClasses.size() == 0 ? null : phpClasses.iterator().next();
    }

    @Nullable
    public static <T extends PsiElement> T getChildrenOnPatternMatch(@Nullable PsiElement element, ElementPattern<T> pattern) {
        if (element == null) return null;

        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (pattern.accepts(child)) {
                //noinspection unchecked
                return (T)child;
            }
        }

        return null;
    }

    public static PhpClass getClassInsideAnnotation(StringLiteralExpression phpDocString) {
        return getClassInsideAnnotation(phpDocString, phpDocString.getContents());
    }

    public static PhpClass getClassInsideAnnotation(StringLiteralExpression phpDocString, String modelName) {

        // \ns\Class fine we dont need to resolve classname we are in global context
        if(modelName.startsWith("\\")) {
            return PhpElementsUtil.getClassInterface(phpDocString.getProject(), modelName);
        }

        // try class shortcut: ns\Class
        PhpClass phpClass = PhpElementsUtil.getClassInterface(phpDocString.getProject(), modelName);
        if(phpClass != null) {
            return phpClass;
        }

        PhpDocComment inClass = PsiTreeUtil.getParentOfType(phpDocString, PhpDocComment.class);
        if(inClass == null) {
            return null;
        }

        // doc before class
        PhpPsiElement phpClassElement = inClass.getNextPsiSibling();
        if(phpClassElement instanceof PhpClass) {
            String className = ((PhpClass) phpClassElement).getNamespaceName() + modelName;
            return PhpElementsUtil.getClassInterface(phpDocString.getProject(), className);
        }

        // eg property, method
        PhpClass insidePhpClass = PsiTreeUtil.getParentOfType(phpClassElement, PhpClass.class);
        if(insidePhpClass != null) {
            String className = insidePhpClass.getNamespaceName() + modelName;
            return PhpElementsUtil.getClassInterface(phpDocString.getProject(), className);
        }

        return null;

    }

}
