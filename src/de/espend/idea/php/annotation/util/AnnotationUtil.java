package de.espend.idea.php.annotation.util;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import de.espend.idea.php.annotation.AnnotationStubIndex;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagGotoHandler;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.dict.PhpAnnotation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationUtil {

    public static final ExtensionPointName<PhpAnnotationCompletionProvider> EXTENSION_POINT_COMPLETION = new ExtensionPointName<PhpAnnotationCompletionProvider>("de.espend.idea.php.annotation.PhpAnnotationCompletionProvider");
    public static final ExtensionPointName<PhpAnnotationReferenceProvider> EXTENSION_POINT_REFERENCES = new ExtensionPointName<PhpAnnotationReferenceProvider>("de.espend.idea.php.annotation.PhpAnnotationReferenceProvider");

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

        CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(project, AnnotationStubIndex.KEY);
        FileBasedIndex.getInstance().processAllKeys(AnnotationStubIndex.KEY, ymlProjectProcessor, project);

        for(String phpClassName: ymlProjectProcessor.getResult()) {
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

    public static class CollectProjectUniqueKeys implements Processor<String> {

        final Project project;
        final ID id;

        final Set<String> stringSet;

        public CollectProjectUniqueKeys(Project project, ID id) {
            this.project = project;
            this.id = id;
            this.stringSet = new HashSet<String>();
        }

        @Override
        public boolean process(String s) {
            this.stringSet.add(s);
            return true;
        }

        public Set<String> getResult() {
            Set<String> set = new HashSet<String>();

            for (String key : stringSet) {
                Collection fileCollection = FileBasedIndex.getInstance().getContainingFiles(id, key, GlobalSearchScope.projectScope(project));

                if (fileCollection.size() > 0) {
                    set.add(key);
                }

            }

            return set;
        }

    }

}

