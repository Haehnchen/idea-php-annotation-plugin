package de.espend.idea.php.annotation.util;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.php.annotation.AnnotationStubIndex;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagGotoHandler;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.dict.AnnotationTarget;
import de.espend.idea.php.annotation.dict.PhpAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationUtil {

    public static final ExtensionPointName<PhpAnnotationCompletionProvider> EXTENSION_POINT_COMPLETION = new ExtensionPointName<PhpAnnotationCompletionProvider>("de.espend.idea.php.annotation.PhpAnnotationCompletionProvider");
    public static final ExtensionPointName<PhpAnnotationReferenceProvider> EXTENSION_POINT_REFERENCES = new ExtensionPointName<PhpAnnotationReferenceProvider>("de.espend.idea.php.annotation.PhpAnnotationReferenceProvider");

    public static final ExtensionPointName<PhpAnnotationDocTagGotoHandler> EP_DOC_TAG_GOTO = new ExtensionPointName<PhpAnnotationDocTagGotoHandler>("de.espend.idea.php.annotation.PhpAnnotationDocTagGotoHandler");
    public static final ExtensionPointName<PhpAnnotationDocTagAnnotator> EP_DOC_TAG_ANNOTATOR = new ExtensionPointName<PhpAnnotationDocTagAnnotator>("de.espend.idea.php.annotation.PhpAnnotationDocTagAnnotator");

    public static Set<String> NON_ANNOTATION_TAGS = new HashSet<String>() {{
        addAll(Arrays.asList(PhpDocUtil.ALL_TAGS));
        add("@Annotation");
        add("@inheritDoc");
        add("@Enum");
        add("@inheritdoc");
        add("@Target");
    }};

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

    @NotNull
    public static Map<String, PhpAnnotation> getAnnotationsOnTargetMap(Project project, AnnotationTarget... targets) {

        Map<String, PhpAnnotation> phpAnnotations = new HashMap<String, PhpAnnotation>();

        for(PhpClass phpClass: AnnotationUtil.getAnnotationsClasses(project)) {
            PhpAnnotation phpAnnotation = AnnotationUtil.getClassAnnotation(phpClass);
            if(phpAnnotation != null && phpAnnotation.hasTarget(targets)) {
                String presentableFQN = phpClass.getPresentableFQN();
                if(presentableFQN != null) {
                    phpAnnotations.put(presentableFQN, phpAnnotation);
                }
            }

        }

        return phpAnnotations;

    }

    @NotNull
    public static Map<String, String> getUseImportMap(@Nullable PhpDocTag phpDocTag) {
        return getUseImportMap(PsiTreeUtil.getParentOfType(phpDocTag, PhpDocComment.class));
    }

    /*
    * Collect file use imports and resolve alias with their class name
    *
    * @param PhpDocComment current doc scope
    * @return map with class names as key and fqn on value
    */
    @NotNull
    public static Map<String, String> getUseImportMap(@Nullable PhpDocComment phpDocComment) {

        // search for use alias in local file
        final Map<String, String> useImports = new HashMap<String, String>();

        if(phpDocComment == null) {
            return useImports;
        }

        GroupStatement phpNamespace = PsiTreeUtil.getParentOfType(phpDocComment, GroupStatement.class);
        if(phpNamespace == null) {
            return useImports;
        }

        for(PhpUseList phpUseList : PsiTreeUtil.getChildrenOfTypeAsList(phpNamespace, PhpUseList.class)) {
            PhpUse[] declarations = phpUseList.getDeclarations();
            if(declarations != null) {
                for(PhpUse phpUse : declarations) {
                    String alias = phpUse.getAliasName();
                    if (alias != null) {
                        useImports.put(alias, phpUse.getOriginal());
                    } else {
                        useImports.put(phpUse.getName(), phpUse.getOriginal());
                    }
                }
            }
        }

        return useImports;
    }

    @Nullable
    public static PhpClass getAnnotationReference(@Nullable PhpDocTag phpDocTag) {
        if(phpDocTag == null) return null;

        // check annoation in current namespace
        String tagName = phpDocTag.getName();
        if(tagName.startsWith("@")) {
            tagName = tagName.substring(1);
        }

        PhpNamespace phpNamespace = PsiTreeUtil.getParentOfType(phpDocTag, PhpNamespace.class);
        if(phpNamespace != null) {
            String currentNsClass = phpNamespace.getFQN() + "\\" + tagName;
            PhpClass phpClass = PhpElementsUtil.getClass(phpDocTag.getProject(), currentNsClass);
            if(phpClass != null) {
                return phpClass;
            }
        }

        return getAnnotationReference(phpDocTag, getUseImportMap(phpDocTag));

    }

    @Nullable
    public static PhpClass getAnnotationReference(PhpDocTag phpDocTag, final Map<String, String> useImports) {

        String tagName = phpDocTag.getName();
        if(tagName.startsWith("@")) {
            tagName = tagName.substring(1);
        }

        String className = tagName;
        String subNamespaceName = "";
        if(className.contains("\\")) {
            className = className.substring(0, className.indexOf("\\"));
            subNamespaceName = tagName.substring(className.length());
        }

        if(!useImports.containsKey(className)) {

            // allow full classes on annotations #17 eg: @Doctrine\ORM\Mapping\PostPersist()
            PhpClass phpClass = PhpElementsUtil.getClass(phpDocTag.getProject(), tagName);
            if(phpClass != null && isAnnotationClass(phpClass)) {
                return phpClass;
            }

            return null;
        }

        return PhpElementsUtil.getClass(phpDocTag.getProject(), useImports.get(className) + subNamespaceName);

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
                Collection fileCollection = FileBasedIndex.getInstance().getContainingFiles(id, key, GlobalSearchScope.allScope(this.project));

                if (fileCollection.size() > 0) {
                    set.add(key);
                }

            }

            return set;
        }

    }

    public static boolean isValidForIndex(FileContent inputData) {

        String fileName = inputData.getPsiFile().getName();
        if(fileName.startsWith(".") || fileName.contains("Test")) {
            return false;
        }

        // is Test file in path name
        String relativePath = VfsUtil.getRelativePath(inputData.getFile(), inputData.getProject().getBaseDir(), '/');
        if(relativePath == null || relativePath.contains("\\Test") || relativePath.contains("\\Fixtures")) {
            return false;
        }

        return true;
    }

    @Nullable
    public static PhpDocCommentAnnotation getPhpDocCommentAnnotationContainer(@Nullable PhpDocComment phpDocComment) {
        if(phpDocComment == null) return null;

        Map<String, String> uses = AnnotationUtil.getUseImportMap(phpDocComment);

        Map<String, PhpDocTagAnnotation> annotationRefsMap = new HashMap<String, PhpDocTagAnnotation>();
        for(PhpDocTag phpDocTag: PsiTreeUtil.findChildrenOfType(phpDocComment, PhpDocTag.class)) {
            if(!AnnotationUtil.NON_ANNOTATION_TAGS.contains(phpDocTag.getName())) {
                PhpClass annotationClass = AnnotationUtil.getAnnotationReference(phpDocTag, uses);
                if(annotationClass != null && annotationClass.getPresentableFQN() != null) {
                    annotationRefsMap.put(annotationClass.getPresentableFQN(), new PhpDocTagAnnotation(annotationClass, phpDocTag));
                }
            }

        }

        return new PhpDocCommentAnnotation(annotationRefsMap, phpDocComment);
    }

    @Nullable
    public static PhpDocTagAnnotation getPhpDocAnnotationContainer(@NotNull PhpDocTag phpDocTag) {

        PhpClass annotationReference = getAnnotationReference(phpDocTag);
        if(annotationReference == null) {
            return null;
        }

        return new PhpDocTagAnnotation(annotationReference, phpDocTag);
    }

}


