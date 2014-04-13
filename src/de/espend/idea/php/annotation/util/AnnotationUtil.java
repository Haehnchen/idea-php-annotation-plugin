package de.espend.idea.php.annotation.util;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import de.espend.idea.php.annotation.AnnotationStubIndex;
import de.espend.idea.php.annotation.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.PhpAnnotationDocTagGotoHandler;
import de.espend.idea.php.annotation.PhpAnnotationReferencesProvider;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.dict.PhpAnnotation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationUtil {

    public static final ExtensionPointName<PhpAnnotationCompletionProvider> EXTENSION_POINT_COMPLETION = new ExtensionPointName<PhpAnnotationCompletionProvider>("de.espend.idea.php.annotation.PhpAnnotationCompletionProvider");
    public static final ExtensionPointName<PhpAnnotationReferencesProvider> EXTENSION_POINT_REFERENCES = new ExtensionPointName<PhpAnnotationReferencesProvider>("de.espend.idea.php.annotation.PhpAnnotationReferencesProvider");

    public static final ExtensionPointName<PhpAnnotationDocTagGotoHandler> EP_DOC_TAG_GOTO = new ExtensionPointName<PhpAnnotationDocTagGotoHandler>("de.espend.idea.php.annotation.PhpAnnotationDocTagGotoHandler");

    public static boolean isAnnotationClass(PhpClass phpClass) {
        PhpDocComment phpDocComment = phpClass.getDocComment();
        if(phpDocComment != null) {
            PhpDocTag[] annotationDocTags = phpDocComment.getTagElementsByName("@Annotation");
            if(annotationDocTags.length > 0) {
                return true;
            }
        }

        return false;
    }

    public static PhpClass[] getAnnotationsClasses(Project project) {
        ArrayList<PhpClass> phpClasses = new ArrayList<PhpClass>();

        Collection<String> phpNamedClasses = FileBasedIndexImpl.getInstance().getAllKeys(AnnotationStubIndex.KEY, project);
        for(String phpClassName: phpNamedClasses) {
            PhpClass phpClass = PhpElementsUtil.getClass(project, phpClassName);
            if(phpClass != null) {
                phpClasses.add(phpClass);
            }

        }

        return phpClasses.toArray(new PhpClass[phpClasses.size()]);
    }

    @Nullable
    public static PhpAnnotation getClassAnnotation(PhpClass phpClass) {
        if(!AnnotationUtil.isAnnotationClass(phpClass)) {
            return null;
        }

        PhpDocComment phpDocComment = phpClass.getDocComment();
        if(phpDocComment == null) {
            return null;
        }

        PhpDocTag[] targetTag = phpDocComment.getTagElementsByName("@Target");
        if(targetTag.length == 0) {
            return new PhpAnnotation(phpClass, AnnotationTarget.UNDEFINED);
        }

        ArrayList<AnnotationTarget> targets = new ArrayList<AnnotationTarget>();

        // @Target("PROPERTY", "METHOD")
        // @Target("CLASS")
        // @Target("ALL")
        Pattern targetPattern = Pattern.compile("\"(\\w+)\"");

        // @TODO: remove on stable api
        // getTagValue is empty on eap; fallback to text
        String tagValue = targetTag[0].getTagValue();
        if(tagValue.length() == 0) {
            tagValue = targetTag[0].getText();
        }

        Matcher matcher = targetPattern.matcher(tagValue);

        while (matcher.find()) {
            try {
                targets.add(AnnotationTarget.valueOf(matcher.group(1).toUpperCase()));
            } catch (IllegalArgumentException e) {
                targets.add(AnnotationTarget.UNKNOWN);
            }

        }

        return new PhpAnnotation(phpClass, targets);
    }

    public static ArrayList<PhpAnnotation> getAnnotationsOnTarget(Project project, AnnotationTarget... targets) {

        ArrayList<PhpAnnotation> phpAnnotations = new ArrayList<PhpAnnotation>();

        for(PhpClass phpClass: AnnotationUtil.getAnnotationsClasses(project)) {
            PhpAnnotation phpAnnotation = AnnotationUtil.getClassAnnotation(phpClass);
            if(phpAnnotation != null && phpAnnotation.hasTarget(targets)) {
                phpAnnotations.add(phpAnnotation);
            }

        }

        return phpAnnotations;

    }

    public static ArrayList<PhpAnnotation> getAnnotationsOnName(Project project, String annotationName, AnnotationTarget... targets) {
        ArrayList<PhpAnnotation> resultPhpAnnotation = new ArrayList<PhpAnnotation>();

        for(PhpAnnotation phpAnnotation: getAnnotationsOnTarget(project, targets)) {
            if(phpAnnotation.getPhpClass().getName().equals(annotationName)) {
                resultPhpAnnotation.add(phpAnnotation);
            }
        }

        return resultPhpAnnotation;
    }


    @Nullable
    public static PhpClass getAnnotationReference(PhpDocTag phpDocTag) {

        // only usable on eap7 because of "@ORM\OneToMany()" bug before
        String annotationName = phpDocTag.getName().substring(1);

        // @TODO: remove this
        // phpstorm6 fallback;
        String tagText = phpDocTag.getText();
        if(tagText.contains("(")) {
            annotationName = tagText.substring(1, tagText.indexOf("("));
        }

        // search for use alias in local file
        final HashMap<String, String> useImports = new HashMap<String, String>();
        phpDocTag.getContainingFile().acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(element instanceof PhpUse) {
                    visitUse((PhpUse) element);
                }
                super.visitElement(element);
            }

            private void visitUse(PhpUse phpUse) {
                String alias = phpUse.getAliasName();
                if(alias != null) {
                    useImports.put(alias, phpUse.getOriginal());
                } else {
                    useImports.put(phpUse.getName(), phpUse.getOriginal());
                }

            }

        });


        String className = annotationName;
        String subNamespaceName = "";
        if(className.contains("\\")) {
            className = className.substring(0, className.indexOf("\\"));
            subNamespaceName = annotationName.substring(className.length());
        }

        if(!useImports.containsKey(className)) {
            return null;
        }

        String resolvedClassName = useImports.get(className) + subNamespaceName;

        return PhpElementsUtil.getClass(phpDocTag.getProject(), resolvedClassName);

    }

}

