package de.espend.idea.php.annotation.util;

import com.intellij.openapi.project.Project;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.refactoring.PhpAliasImporter;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
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
            return AnnotationTarget.CLASS;
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

    /**
     * Get a PhpClass on some possible context like namespace or use imports
     * TODO there must be some core utils
     *
     */
    @Nullable
    public static PhpClass getClassByContext(PsiElement psiElement, String className) {

        PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag.class);
        if(phpDocTag == null) {
            return null;
        }

        Map<String, String> map = AnnotationUtil.getUseImportMap(phpDocTag);
        if(map.containsKey(className)) {
            return PhpElementsUtil.getClass(psiElement.getProject(), map.get(className));
        }

        PhpNamespace phpNamespace = PsiTreeUtil.getParentOfType(psiElement, PhpNamespace.class);
        if(phpNamespace != null) {
            return PhpElementsUtil.getClass(psiElement.getProject(), phpNamespace.getFQN() + "\\" + className);
        }

        return PhpElementsUtil.getClass(psiElement.getProject(), className);
    }


    @Nullable
    public static String getStringValue(@Nullable PsiElement psiElement) {
        return getStringValue(psiElement, 0);
    }

    @Nullable
    private static String getStringValue(@Nullable PsiElement psiElement, int depth) {

        if(psiElement == null || ++depth > 5) {
            return null;
        }

        if(psiElement instanceof StringLiteralExpression) {
            String resolvedString = ((StringLiteralExpression) psiElement).getContents();
            if(StringUtils.isEmpty(resolvedString)) {
                return null;
            }

            return resolvedString;
        }

        if(psiElement instanceof Field) {
            return getStringValue(((Field) psiElement).getDefaultValue(), depth);
        }

        if(psiElement instanceof PhpReference) {

            PsiReference psiReference = psiElement.getReference();
            if(psiReference == null) {
                return null;
            }

            PsiElement ref = psiReference.resolve();
            if(ref instanceof PhpReference) {
                return getStringValue(psiElement, depth);
            }

            if(ref instanceof Field) {
                PsiElement resolved = ((Field) ref).getDefaultValue();

                if(resolved instanceof StringLiteralExpression) {
                    return ((StringLiteralExpression) resolved).getContents();
                }
            }

        }

        return null;

    }

    /**
     * return 'value' inside class method
     */
    static public ElementPattern<PhpExpression> getMethodReturnPattern() {
        return PlatformPatterns.or(
                PlatformPatterns.psiElement(StringLiteralExpression.class)
                        .withParent(PlatformPatterns.psiElement(PhpReturn.class).inside(Method.class))
                        .withLanguage(PhpLanguage.INSTANCE),
                PlatformPatterns.psiElement(ClassConstantReference.class)
                        .withParent(PlatformPatterns.psiElement(PhpReturn.class).inside(Method.class))
                        .withLanguage(PhpLanguage.INSTANCE)
        );
    }

    /**
     * Find a string return value of a method context "function() { return 'foo'}"
     * First match wins
     */
    @Nullable
    static public String getMethodReturnAsString(@NotNull PhpClass phpClass, @NotNull String methodName) {

        Method method = phpClass.findMethodByName(methodName);
        if(method == null) {
            return null;
        }

        final Set<String> values = new HashSet<String>();
        method.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {

                if(PhpElementsUtil.getMethodReturnPattern().accepts(element)) {
                    String value = PhpElementsUtil.getStringValue(element);
                    if(value != null && StringUtils.isNotBlank(value)) {
                        values.add(value);
                    }
                }

                super.visitElement(element);
            }
        });

        if(values.size() == 0) {
            return null;
        }

        // we support only first item
        return values.iterator().next();
    }

    /**
     * Adds class as alias
     *
     * @param scopeForUseOperator Any element that is inside a namespace statement
     * @param nsClass \Class\Foo
     * @param alias Class\Foo as Bar
     */
    public static void insertUseIfNecessary(@NotNull PhpPsiElement scopeForUseOperator, @NotNull String nsClass, @NotNull String alias) {
        // we need absolute class, else we get duplicate imports
        if(!nsClass.startsWith("\\")) {
            nsClass = "\\" + nsClass;
        }

        if(!PhpCodeInsightUtil.getAliasesInScope(scopeForUseOperator).values().contains(nsClass)) {
            PhpAliasImporter.insertUseStatement(nsClass, alias, scopeForUseOperator);
        }
    }

}
