package de.espend.idea.php.annotation.util;

import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.completion.StartOnlyMatcher;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.TripleFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import de.espend.idea.php.annotation.AnnotationStubIndex;
import de.espend.idea.php.annotation.AnnotationUsageIndex;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.dict.*;
import de.espend.idea.php.annotation.extension.*;
import de.espend.idea.php.annotation.extension.parameter.AnnotationGlobalNamespacesLoaderParameter;
import de.espend.idea.php.annotation.inspection.AnnotationInspectionUtil;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationUtil {
    private static final Key<CachedValue<Map<String, PhpAnnotation>>> ANNOTATION_FQN_MAP_CACHE = new Key<>("ANNOTATION_FQN_MAP_CACHE");
    private static final Pattern ANNOTATION_TARGET_PATTERN = Pattern.compile("\"(\\w+)\"");

    public static final ExtensionPointName<PhpAnnotationCompletionProvider> EXTENSION_POINT_COMPLETION = new ExtensionPointName<>("de.espend.idea.php.annotation.PhpAnnotationCompletionProvider");
    public static final ExtensionPointName<PhpAnnotationReferenceProvider> EXTENSION_POINT_REFERENCES = new ExtensionPointName<>("de.espend.idea.php.annotation.PhpAnnotationReferenceProvider");

    public static final ExtensionPointName<PhpAnnotationDocTagGotoHandler> EP_DOC_TAG_GOTO = new ExtensionPointName<>("de.espend.idea.php.annotation.PhpAnnotationDocTagGotoHandler");
    public static final ExtensionPointName<PhpAnnotationDocTagAnnotator> EP_DOC_TAG_ANNOTATOR = new ExtensionPointName<>("de.espend.idea.php.annotation.PhpAnnotationDocTagAnnotator");
    public static final ExtensionPointName<PhpAnnotationGlobalNamespacesLoader> EXTENSION_POINT_GLOBAL_NAMESPACES = new ExtensionPointName<>("de.espend.idea.php.annotation.PhpAnnotationGlobalNamespacesLoader");

    public static final ExtensionPointName<PhpAnnotationVirtualProperties> EP_VIRTUAL_PROPERTIES = new ExtensionPointName<>("de.espend.idea.php.annotation.PhpAnnotationVirtualProperties");
    public static final ExtensionPointName<PhpAnnotationUseAlias> EP_USE_ALIASES = new ExtensionPointName<>("de.espend.idea.php.annotation.PhpAnnotationUseAlias");

    final private static Set<String> NON_ANNOTATION_TAGS = new HashSet<>() {{
        addAll(Arrays.asList(PhpDocUtil.ALL_TAGS));
        add("@Annotation");
        add("@inheritDoc");
        add("@Enum");
        add("@inheritdoc");
        add("@Target");
        add("@Required");
        add("@Attributes");
        add("@Attribute");
        add("@test");
        add("@noninspection");
        add("@noinspection");
        add("@covers");

        add("@SuppressWarnings");
        add("@package_version");
        add("@startuml");
        add("@enduml");
        add("@experimental");
        add("@codingStandardsIgnoreStart");
        add("@codingStandardsIgnoreEnd");
        add("@phpcsSuppress");
        add("@extends");
        add("@implements");
        add("@template");
        add("@use");
        add("@TODO");

        add("@Given");
        add("@When");
        add("@Then");
    }};

    public static boolean isAnnotationClass(@NotNull PhpClass phpClass) {
        return getClassAnnotationTargets(phpClass) != null;
    }

    public static PhpClass[] getAnnotationsClasses(Project project) {
        return getCachedAnnotationsMap(project)
            .values()
            .stream()
            .map(PhpAnnotation::getPhpClass)
            .toArray(PhpClass[]::new);
    }

    /**
     * Provide a annotation class container. Allows easy access to @Target attributes
     * Array and single value supported: @Target("PROPERTY", "METHOD"), @Target("CLASS")
     */
    @Nullable
    public static PhpAnnotation getClassAnnotation(@NotNull PhpClass phpClass) {
        String fqn = StringUtils.stripStart(phpClass.getFQN(), "\\");
        if (StringUtils.isNotBlank(fqn)) {
            PhpAnnotation cached = getCachedAnnotationsMap(phpClass.getProject()).get(fqn);
            if (cached != null) {
                return cached;
            }
        }

        List<AnnotationTarget> targets = getClassAnnotationTargets(phpClass);
        if (targets == null || targets.isEmpty()) {
            return null;
        }

        return new PhpAnnotation(phpClass, targets);
    }

    @NotNull
    public static Map<String, PhpAnnotation> getAnnotationsOnTargetMap(@NotNull Project project, AnnotationTarget... targets) {
        Map<String, PhpAnnotation> phpAnnotations = new HashMap<>();

        for (Map.Entry<String, PhpAnnotation> entry : getCachedAnnotationsMap(project).entrySet()) {
            PhpAnnotation phpAnnotation = entry.getValue();
            if (phpAnnotation.hasTarget(targets)) {
                String fqn = entry.getKey();
                phpAnnotations.put(fqn, phpAnnotation);
            }
        }

        return phpAnnotations;
    }

    /**
     * Build a cached map of all indexed annotation classes keyed by normalized FQN.
     */
    @NotNull
    public static Map<String, PhpAnnotation> getCachedAnnotationsMap(@NotNull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            ANNOTATION_FQN_MAP_CACHE,
            () -> {
                Map<String, PhpAnnotation> phpAnnotations = new HashMap<>();
                GlobalSearchScope scope = GlobalSearchScope.allScope(project);

                FileBasedIndex.getInstance().processAllKeys(AnnotationStubIndex.KEY, fqn -> {
                    if (StringUtils.isBlank(fqn)) {
                        return true;
                    }

                    PhpClass phpClass = PhpElementsUtil.getClass(project, fqn);
                    if (phpClass == null) {
                        return true;
                    }

                    List<String> values = FileBasedIndex.getInstance().getValues(AnnotationStubIndex.KEY, fqn, scope);
                    if (values.isEmpty()) {
                        return true;
                    }

                    phpAnnotations.put(fqn, new PhpAnnotation(phpClass, getAnnotationTargetsFromSerializedValue(values.get(0))));
                    return true;
                }, project);

                return CachedValueProvider.Result.create(
                    Collections.unmodifiableMap(phpAnnotations),
                    getModificationTrackerForIndexId(project, AnnotationStubIndex.KEY)
                );
            },
            false
        );
    }

    /**
     * @deprecated "Use PsiElement parameter and check for null"
     */
    @Deprecated
    @NotNull
    public static Map<String, String> getUseImportMap(@Nullable PhpDocTag phpDocTag) {
        return getUseImportMap((PsiElement) phpDocTag);
    }

    /**
     * @deprecated "Use PsiElement parameter and check for null"
     */
    @Deprecated
    public static Map<String, String> getUseImportMap(@Nullable PhpDocComment phpDocComment) {
        return getUseImportMap((PsiElement) phpDocComment);
    }

    /*
     * Collect file use imports and resolve alias with their class name
     *
     * @param PhpDocComment current doc scope
     * @return map with class names as key and fqn on value
     */
    @NotNull
    public static Map<String, String> getUseImportMap(@NotNull PsiElement phpDocComment) {
        PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(phpDocComment);
        if(scope == null) {
            return Collections.emptyMap();
        }

        Map<String, String> useImports = new HashMap<>();

        for (PhpUseList phpUseList : PhpCodeInsightUtil.collectImports(scope)) {
            for(PhpUse phpUse : phpUseList.getDeclarations()) {
                String alias = phpUse.getAliasName();
                useImports.put(Objects.requireNonNullElseGet(alias, phpUse::getName), phpUse.getFQN());
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

        return getAnnotationReference(phpDocTag, getUseImportMap((PsiElement) phpDocTag));

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

            // global namespace support
            AnnotationGlobalNamespacesLoaderParameter parameter = null;
            for (PhpAnnotationGlobalNamespacesLoader loader : EXTENSION_POINT_GLOBAL_NAMESPACES.getExtensions()) {
                if(parameter == null) {
                    parameter = new AnnotationGlobalNamespacesLoaderParameter(phpDocTag.getProject());
                }

                for (String ns : loader.getGlobalNamespaces(parameter)) {
                    PhpClass globalPhpClass = PhpElementsUtil.getClassInterface(phpDocTag.getProject(), ns + "\\" + className);
                    if(globalPhpClass != null) {
                        return globalPhpClass;
                    }
                }
            }

            return null;
        }

        return PhpElementsUtil.getClass(phpDocTag.getProject(), useImports.get(className) + subNamespaceName);

    }

    /**
     * "\My\Bar::cla<caret>ss" => "\My\Bar"
     */
    @Nullable
    public static PhpClass getClassFromConstant(@NotNull PsiElement psiElement) {
        PsiElement prevSibling = psiElement.getPrevSibling();

        if (prevSibling == null || !de.espend.idea.php.annotation.util.PhpDocUtil.isDocStaticElement(prevSibling)) {
            return null;
        }

        PsiElement prevSibling1 = prevSibling.getPrevSibling();
        if (prevSibling1 == null || prevSibling1.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER) {
            return null;
        }

        return getClassFromDocIdentifier(prevSibling1);
    }

    /**
     * "\My\B<caret>ar::class" => "\My\Bar"
     */
    @Nullable
    public static PhpClass getClassFromDocIdentifier(@NotNull PsiElement psiElement) {
        String classFromDocIdentifierAsString = getClassFromDocIdentifierAsString(psiElement);
        if (classFromDocIdentifierAsString == null) {
            return null;
        }

        return PhpElementsUtil.getClassInterface(psiElement.getProject(), classFromDocIdentifierAsString);
    }

    /**
     * "\My\B<caret>ar::class" => "\My\Bar"
     */
    @Nullable
    public static String getClassFromDocIdentifierAsString(@NotNull PsiElement psiElement) {
        if(psiElement.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER) {
            return null;
        }

        String namespaceForDocIdentifier = de.espend.idea.php.annotation.util.PhpDocUtil.getNamespaceForDocIdentifier(psiElement);
        if (namespaceForDocIdentifier == null) {
            return null;
        }

        return AnnotationInspectionUtil.getClassFqnString(namespaceForDocIdentifier, new AnnotationInspectionUtil.LazyNamespaceImportResolver(psiElement));
    }

    public static class CollectProjectUniqueKeys implements Processor<String> {

        final Project project;
        final ID id;

        final Set<String> stringSet;

        public CollectProjectUniqueKeys(Project project, ID id) {
            this.project = project;
            this.id = id;
            this.stringSet = new HashSet<>();
        }

        @Override
        public boolean process(String s) {
            this.stringSet.add(s);
            return true;
        }

        public Set<String> getResult() {
            Set<String> set = new HashSet<>();

            for (String key : stringSet) {
                Collection fileCollection = FileBasedIndex.getInstance().getContainingFiles(id, key, GlobalSearchScope.allScope(this.project));

                if (!fileCollection.isEmpty()) {
                    set.add(key);
                }

            }

            return set;
        }

    }

    public static boolean isValidForIndex(@NotNull FileContent inputData) {
        String fileName = inputData.getPsiFile().getName();
        if(fileName.startsWith(".") || fileName.contains("Test")) {
            return false;
        }

        // we check for project path, on no match we are properly inside external library paths
        VirtualFile baseDir = inputData.getProject().getBaseDir();
        if (baseDir == null) {
            return true;
        }

        String relativePath = VfsUtil.getRelativePath(inputData.getFile(), baseDir, '/');
        if(relativePath == null) {
            return true;
        }

        // is Test file in path name
        return !(relativePath.contains("\\Test\\") || relativePath.contains("\\Fixtures\\"));
    }

    @Nullable
    public static PhpDocCommentAnnotation getPhpDocCommentAnnotationContainer(@Nullable PhpDocComment phpDocComment) {
        if(phpDocComment == null) {
            return null;
        }

        Map<String, String> uses = AnnotationUtil.getUseImportMap((PsiElement) phpDocComment);

        Map<String, PhpDocTagAnnotation> annotationRefsMap = new HashMap<>();
        for(PhpDocTag phpDocTag: PsiTreeUtil.findChildrenOfType(phpDocComment, PhpDocTag.class)) {
            if(!AnnotationUtil.isBlockedAnnotationTag(phpDocTag.getName())) {
                PhpClass annotationClass = AnnotationUtil.getAnnotationReference(phpDocTag, uses);
                if(annotationClass != null) {
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

    public static boolean isAnnotationPhpDocTag(PhpDocTag phpDocTag) {
        PhpDocComment phpDocComment = PsiTreeUtil.getParentOfType(phpDocTag, PhpDocComment.class);
        if(phpDocComment == null) {
            return false;
        }

        PsiElement nextPsiElement = phpDocComment.getNextPsiSibling();
        return nextPsiElement != null
            && (nextPsiElement instanceof Method || nextPsiElement instanceof PhpClass || nextPsiElement.getNode().getElementType() == PhpElementTypes.CLASS_FIELDS);
    }

    public static Map<String, String> getPossibleImportClasses(@NotNull PhpDocTag phpDocTag) {
        String className = phpDocTag.getName();
        if (className.startsWith("@")) {
            className = className.substring(1);
        }

        Map<String, String> phpClasses = new HashMap<>();

        // "@Test\Bar" can be ignored here
        // "@Bar" We only search for a direct class
        if (!className.contains("\\")) {
            for (PhpClass annotationClass: AnnotationUtil.getAnnotationsClasses(phpDocTag.getProject())) {
                if (annotationClass.getName().equals(className)) {
                    phpClasses.put(annotationClass.getFQN(), null);
                }
            }
        }

        // "@ORM\Entity" is search for configured alias which can also provide a valid "use" path
        if (!className.startsWith("\\")) {
            String[] split = className.split("\\\\");
            if (split.length > 1) {
                String aliasStart = split[0];

                for (UseAliasOption useAliasOption : AnnotationUtil.getActiveImportsAliasesFromSettings()) {
                    String alias = useAliasOption.getAlias();
                    if (!aliasStart.equals(alias)) {
                        continue;
                    }

                    String clazz = useAliasOption.getClassName() + "\\" + StringUtils.join(Arrays.copyOfRange(split, 1, split.length), "\\");
                    PhpClass phpClass = PhpElementsUtil.getClassInterface(phpDocTag.getProject(), clazz);

                    if (phpClass != null && isAnnotationClass(phpClass)) {
                        // user input for class name
                        phpClasses.put("\\" + StringUtils.stripStart(useAliasOption.getClassName(), "\\"), alias);
                    }
                }
            }
        }

        return phpClasses;
    }

    /**
     * '@Foo(name={"FOOBAR", "FOOBAR2"})'
     */
    @Nullable
    public static PsiElement getPropertyForArray(@NotNull StringLiteralExpression psiElement) {
        return PhpElementsUtil.getPrevSiblingOfPatternMatch(psiElement, AnnotationPattern.getPropertyNameOfArrayValuePattern());
    }

    /**
     * Extract property value or fallback on default annotation pattern
     *
     * "@Template("foobar.html.twig")"
     * "@Template(template="foobar.html.twig")"
     */
    @Nullable
    public static String getPropertyValueOrDefault(@NotNull PhpDocTag phpDocTag, @NotNull String property) {
        PhpPsiElement attributeList = phpDocTag.getFirstPsiChild();
        if(attributeList == null || attributeList.getNode().getElementType() != PhpDocElementTypes.phpDocAttributeList) {
            return null;
        }

        // @Template(template="foobar.html.twig")
        PsiElementPattern.Capture<StringLiteralExpression> propertyPattern = getPropertyIdentifierValue(property);
        PsiElement psiProperty = Arrays.stream(attributeList.getChildren())
            .filter(psiElement1 -> propertyPattern.accepts(psiElement1))
            .findFirst()
            .orElse(null);

        String contents = null;

        // find property value: template="foobar.html.twig"
        if(psiProperty != null) {
            if(!(psiProperty instanceof StringLiteralExpression)) {
                return null;
            }

            contents = ((StringLiteralExpression) psiProperty).getContents();
        }

        // default value: @Template("foobar.html.twig")
        if(contents == null) {
            PsiElement defaultItem = attributeList.getFirstChild();
            if(defaultItem != null) {
                PsiElement defaultValue = defaultItem.getNextSibling();
                if(defaultValue instanceof StringLiteralExpression) {
                    contents = ((StringLiteralExpression) defaultValue).getContents();
                }
            }
        }

        if(StringUtils.isNotBlank(contents)) {
            return contents;
        }

        return null;
    }

    /**
     * Get the property value by given name
     *
     * "@Template(template="foobar.html.twig")"
     */
    @Nullable
    public static String getPropertyValue(@NotNull PhpDocTag phpDocTag, @NotNull String property) {
        PhpPsiElement attributeList = phpDocTag.getFirstPsiChild();
        if(attributeList == null || attributeList.getNode().getElementType() != PhpDocElementTypes.phpDocAttributeList) {
            return null;
        }

        PsiElement lParen = attributeList.getFirstChild();
        if(lParen == null) {
            return null;
        }

        StringLiteralExpression psiProperty = getPropertyValueAsPsiElement(phpDocTag, property);
        if(psiProperty == null) {
            return null;
        }

        String contents = psiProperty.getContents();
        if(StringUtils.isNotBlank(contents)) {
            return contents;
        }

        return null;
    }

    /**
     * Get the property value as string by given name
     *
     * "@Template(template="foobar.html.twig")"
     */
    @Nullable
    public static StringLiteralExpression getPropertyValueAsPsiElement(@NotNull PhpDocTag phpDocTag, @NotNull String property) {
        PhpPsiElement attributeList = phpDocTag.getFirstPsiChild();
        if(attributeList == null || attributeList.getNode().getElementType() != PhpDocElementTypes.phpDocAttributeList) {
            return null;
        }

        PsiElement lParen = attributeList.getFirstChild();
        if(lParen == null) {
            return null;
        }

        PsiElementPattern.Capture<StringLiteralExpression> propertyPattern = getPropertyIdentifierValue(property);
        PsiElement psiProperty = Arrays.stream(attributeList.getChildren())
            .filter(psiElement1 -> propertyPattern.accepts(psiElement1))
            .findFirst()
            .orElse(null);

        return psiProperty instanceof StringLiteralExpression ?
            (StringLiteralExpression) psiProperty :
            null;
    }

    /**
     * Get the property value as PsiElement by given name
     *
     * "@Template(template=Foobar::class)"
     */
    @Nullable
    public static PsiElement getPropertyValueAsElement(@NotNull PhpDocTag phpDocTag, @NotNull String property) {
        PhpPsiElement attributeList = phpDocTag.getFirstPsiChild();
        if(attributeList == null || attributeList.getNode().getElementType() != PhpDocElementTypes.phpDocAttributeList) {
            return null;
        }

        PsiElement lParen = attributeList.getFirstChild();
        if(lParen == null) {
            return null;
        }

        PsiElementPattern.Capture<PsiElement> propertyPattern = getPropertyIdentifierValueAsPsiElement(property);
        return Arrays.stream(attributeList.getChildren())
            .filter(psiElement1 -> propertyPattern.accepts(psiElement1))
            .findFirst()
            .orElse(null);
    }

    @NotNull
    private static Collection<PsiFile> getFilesImplementingAnnotation(@NotNull Project project, @NotNull String phpClassName) {
        Collection<VirtualFile> files = new HashSet<>();

        FileBasedIndex.getInstance().getFilesWithKey(AnnotationUsageIndex.KEY, new HashSet<>(Collections.singletonList(phpClassName)), virtualFile -> {
            files.add(virtualFile);
            return true;
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), PhpFileType.INSTANCE));

        Collection<PsiFile> elements = new ArrayList<>();

        for (VirtualFile file : files) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

            if(psiFile == null) {
                continue;
            }

            elements.add(psiFile);
        }

        return elements;
    }

    /**
     * Find all annotation usages for given class name
     *
     * Doctrine\ORM\Mapping\Entity => ORM\Entity(), Entity()
     *
     * @param project current Project
     * @param fqnClassName Foobar\ClassName
     * @return targets
     */
    public static Collection<PsiElement> getImplementationsForAnnotation(@NotNull Project project, @NotNull String fqnClassName) {
        Collection<PsiElement> psiElements = new HashSet<>();

        for (PsiFile psiFile : getFilesImplementingAnnotation(project, fqnClassName)) {
            PhpDocTagAnnotationVisitorUtil.visitElement((PhpFile) psiFile, pair -> {
                if(StringUtils.stripStart(pair.getFirst(), "\\").equalsIgnoreCase(StringUtils.stripStart(fqnClassName, "\\"))) {
                    psiElements.add(pair.getSecond());
                }

                return true;
            });
        }

        return psiElements;
    }

    /**
     * Serialize parsed annotation targets for storage in the file index.
     *
     * @return comma-separated enum names or null if the class is not an annotation
     */
    @Nullable
    public static String getSerializedAnnotationTargets(@NotNull PhpClass phpClass) {
        List<AnnotationTarget> targets = getClassAnnotationTargets(phpClass);
        if (targets == null || targets.isEmpty()) {
            return null;
        }

        return serializeAnnotationTargets(targets);
    }

    /**
     * Restore annotation targets from the file index value.
     */
    @NotNull
    public static List<AnnotationTarget> getAnnotationTargetsFromSerializedValue(@NotNull String serializedValue) {
        if (StringUtils.isBlank(serializedValue)) {
            return Collections.singletonList(AnnotationTarget.UNDEFINED);
        }

        List<AnnotationTarget> targets = new ArrayList<>();
        for (String value : StringUtils.split(serializedValue, ',')) {
            try {
                targets.add(AnnotationTarget.valueOf(value));
            } catch (IllegalArgumentException ignored) {
                targets.add(AnnotationTarget.UNKNOWN);
            }
        }

        return targets.isEmpty() ? Collections.singletonList(AnnotationTarget.UNDEFINED) : targets;
    }

    @NotNull
    public static Collection<UseAliasOption> getActiveImportsAliasesFromSettings() {
        Collection<UseAliasOption> useAliasOptions = ApplicationSettings.getUseAliasOptionsWithDefaultFallback();
        if(useAliasOptions.isEmpty()) {
            return Collections.emptyList();
        }

        return ContainerUtil.filter(useAliasOptions, UseAliasOption::isEnabled);
    }

    /**
     * Get attributes for @Foo("test", ATTR<caret>IBUTE)
     *
     * "@Attributes(@Attribute("accessControl", type="string"))"
     * "@Attributes({@Attribute("accessControl", type="string")})"
     * "class foo { public $foo; }"
     */
    public static void visitAttributes(@NotNull PhpClass phpClass, TripleFunction<String, String, PsiElement, Void> fn) {
        for (Field field : phpClass.getFields()) {
            if(field.isConstant()) {
                continue;
            }

            String type = null;
            for (String type2 : field.getType().filterNull().getTypes()) {
                if (PhpType.isPrimitiveType(type2)) {
                    type = StringUtils.stripStart(type2, "\\");
                }
            }

            fn.fun(field.getName(), type, field);
        }

        PhpDocComment docComment = phpClass.getDocComment();
        if (docComment != null) {
            for (PhpDocTag phpDocTag : docComment.getTagElementsByName("@Attributes")) {
                for (PhpDocTag docTag : PsiTreeUtil.collectElementsOfType(phpDocTag, PhpDocTag.class)) {
                    String name = docTag.getName();
                    if (!"@Attribute".equals(name)) {
                        continue;
                    }

                    String defaultPropertyValue = AnnotationUtil.getDefaultPropertyValue(docTag);
                    if (defaultPropertyValue == null || StringUtils.isBlank(defaultPropertyValue)) {
                        continue;
                    }

                    fn.fun(defaultPropertyValue, AnnotationUtil.getPropertyValue(docTag, "type"), docTag);
                }
            }
        }
    }

    /**
     * Parse annotation targets directly from the class docblock.
     *
     * Returns null when the class is not marked as an annotation.
     */
    @Nullable
    private static List<AnnotationTarget> getClassAnnotationTargets(@NotNull PhpClass phpClass) {
        PhpDocComment phpDocComment = phpClass.getDocComment();
        if (phpDocComment == null || phpDocComment.getTagElementsByName("@Annotation").length == 0) {
            return null;
        }

        List<AnnotationTarget> targets = new ArrayList<>();
        PhpDocTag[] tagElementsByName = phpDocComment.getTagElementsByName("@Target");

        if (tagElementsByName.length > 0) {
            for (PhpDocTag phpDocTag : tagElementsByName) {
                Matcher matcher = ANNOTATION_TARGET_PATTERN.matcher(phpDocTag.getText());
                boolean isMatched = false;

                while (matcher.find()) {
                    isMatched = true;
                    try {
                        targets.add(AnnotationTarget.valueOf(matcher.group(1).toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        targets.add(AnnotationTarget.UNKNOWN);
                    }
                }

                if (!isMatched) {
                    targets.add(AnnotationTarget.UNKNOWN);
                }
            }
        } else {
            targets.add(AnnotationTarget.UNDEFINED);
        }

        return targets.isEmpty() ? null : targets;
    }

    @NotNull
    private static String serializeAnnotationTargets(@NotNull List<AnnotationTarget> targets) {
        return targets.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    /**
     * Provide a modification tracker for a concrete file index.
     *
     * This is used by project-level caches that derive their content from indexed values.
     */
    @NotNull
    public static ModificationTracker getModificationTrackerForIndexId(@NotNull Project project, @NotNull final ID<?, ?> id) {
        return () -> FileBasedIndex.getInstance().getIndexModificationStamp(id, project);
    }


    /**
     * matches "@Callback(propertyName="<value>")"
     */
    private static PsiElementPattern.Capture<StringLiteralExpression> getPropertyIdentifierValue(@NotNull String propertyName) {
        return PlatformPatterns.psiElement(StringLiteralExpression.class)
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("=")
                ),
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).withText(propertyName)
            )
            .withParent(PlatformPatterns.psiElement(PhpDocElementTypes.phpDocAttributeList));
    }

    /**
     * matches "@Callback(propertyName="<value>")"
     */
    private static PsiElementPattern.Capture<PsiElement> getPropertyIdentifierValueAsPsiElement(@NotNull String propertyName) {
        return PlatformPatterns.psiElement()
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class),
                    PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_TEXT).withText("=")
                ),
                PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).withText(propertyName)
            )
            .withParent(PlatformPatterns.psiElement(PhpDocElementTypes.phpDocAttributeList));
    }

    /**
     * Get default property value from annotation "@Template("foo");
     *
     * @return Content of property value literal
     */
    @Nullable
    public static String getDefaultPropertyValue(@NotNull PhpDocTag phpDocTag) {
        PhpPsiElement phpDocAttrList = phpDocTag.getFirstPsiChild();

        if(phpDocAttrList != null) {
            if(phpDocAttrList.getNode().getElementType() == PhpDocElementTypes.phpDocAttributeList) {
                PhpPsiElement phpPsiElement = phpDocAttrList.getFirstPsiChild();
                if(phpPsiElement instanceof StringLiteralExpression) {
                    return ((StringLiteralExpression) phpPsiElement).getContents();
                }
            }
        }

        return null;
    }

    /**
     * Check if "@TagName" is need to check for annotation related stuff
     *
     * https://github.com/doctrine/annotations/blob/master/lib/Doctrine/Common/Annotations/ImplicitlyIgnoredAnnotationNames.php
     *
     * @param tagName include "@" at beginning
     */
    public static boolean isBlockedAnnotationTag(@NotNull String tagName) {
        if (NON_ANNOTATION_TAGS.contains(tagName)) {
            return true;
        }

        String lowercase = tagName.toLowerCase();
        return lowercase.startsWith("@phpstan-")
            || lowercase.startsWith("@psalm-")
            || lowercase.startsWith("@phan-")
            || lowercase.startsWith("@phpcs")
            || lowercase.startsWith("@before") // BeforeSuite, BeforeScenario
            || lowercase.startsWith("@after"); // AfterSuite, AfterScenario
    }

    public static void insertNamedArgumentForAnnotation(@NotNull Editor editor, @NotNull PhpDocTag phpDocTag, @NotNull String namedArgument, @NotNull String value) {
        PsiElement lastChild = phpDocTag.getLastChild();
        if (lastChild == null) {
            return;
        }

        if (getDefaultPropertyValue(phpDocTag) != null) {
            String attr = MessageFormat.format(", {0}={1}", namedArgument, value);
            editor.getDocument().insertString(lastChild.getTextRange().getEndOffset() - 1, attr);
        } else {
            int endOffset = lastChild.getTextRange().getEndOffset();

            // append "()"
            if (!phpDocTag.getText().endsWith("()")) {
                editor.getDocument().insertString(endOffset, "()");
                endOffset = endOffset + 2;
            }

            String attr = MessageFormat.format("{0}={1}", namedArgument, value);
            editor.getDocument().insertString(endOffset - 1, attr);
        }
    }

    private enum MetadataType {
        ANNOTATION,
        ATTRIBUTE,
        UNKNOWN
    }

    @NotNull
    private static MetadataType getAnnotationMetadataUsage(@NotNull PsiElement psiElement) {
        final boolean[] hasAnnotation = {false};
        final boolean[] hasAttributes = {false};

        psiElement.acceptChildren(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (hasAnnotation[0] || hasAttributes[0]) {
                    return;
                }

                if (element instanceof PhpDocTag && AnnotationUtil.isAnnotationPhpDocTag((PhpDocTag) element)) {
                    PhpClass phpClass = AnnotationUtil.getAnnotationReference((PhpDocTag) element);
                    if (phpClass != null && phpClass.getFQN().startsWith("\\Doctrine\\ORM")) {
                        hasAnnotation[0] = true;
                        return;
                    }
                }

                if (element instanceof PhpAttribute) {
                    hasAttributes[0] = true;
                    return;
                }

                super.visitElement(element);
            }
        });

        if (hasAttributes[0]) {
            return MetadataType.ATTRIBUTE;
        }

        if (hasAnnotation[0]) {
            return MetadataType.ANNOTATION;
        }

        return MetadataType.UNKNOWN;
    }

    /**
     * Decide on a given file scope if attributes or annotation should be used
     */
    public static boolean useAttributeForGenerateDoctrineMetadata(@NotNull PsiFile file) {
        MetadataType annotationMetadataUsage = getAnnotationMetadataUsage(file);
        if (annotationMetadataUsage != MetadataType.UNKNOWN) {
            return annotationMetadataUsage == MetadataType.ATTRIBUTE;
        }

        for (PhpNamedElement topLevelElement : ((PhpFile) file).getTopLevelDefs().values()) {
            if (topLevelElement instanceof PhpClass phpClass)  {
                String fqn = phpClass.getFQN();

                int i = fqn.lastIndexOf("\\");
                if (i < 1) {
                    continue;
                }

                String parentFqn = fqn.substring(0, i);

                Collection<String> limit = PhpIndex.getInstance(file.getProject())
                    .getAllClassFqns(new StartOnlyMatcher(new PlainPrefixMatcher(parentFqn)))
                    .stream()
                    .sorted(Comparator.comparingDouble(x -> Math.random()))
                    .limit(5)
                    .collect(Collectors.toSet());

                for (String s : limit) {
                    for (PhpClass aClass : PhpIndex.getInstance(file.getProject()).getAnyByFQN(s)) {
                        MetadataType usage = getAnnotationMetadataUsage(aClass.getParent());
                        if (usage != MetadataType.UNKNOWN) {
                            return usage == MetadataType.ATTRIBUTE;
                        }
                    }
                }
            }
        }

        // fallback to project scope via PHP version
        return PhpLanguageLevel.current(file.getProject()).hasFeature(PhpLanguageFeature.ATTRIBUTES);
    }
}
