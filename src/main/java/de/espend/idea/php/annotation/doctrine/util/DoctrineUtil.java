package de.espend.idea.php.annotation.doctrine.util;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.*;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpClassNameIndex;
import de.espend.idea.php.annotation.PhpAnnotationIcons;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineUtil {

    public static final String DOCTRINE_ORM_MAPPING = "\\Doctrine\\ORM\\Mapping";

    final public static String[] DOCTRINE_RELATION_FIELDS = new String[] {
        "\\Doctrine\\ORM\\Mapping\\OneToOne",
        "\\Doctrine\\ORM\\Mapping\\ManyToOne",
        "\\Doctrine\\ORM\\Mapping\\OneToMany",
        "\\Doctrine\\ORM\\Mapping\\ManyToMany",
    };

    private static final Key<CachedValue<Map<String, Set<String>>>> ANNOTATIONS_TARGETS_CACHE = new Key<>("ANNOTATIONS_TARGETS_CACHE");
    private static final Key<CachedValue<Boolean>> IS_DOCTRINE_PROJECT_CACHE = new Key<>("ANNOTATIONS_DOCTRINE_PROJECT");

    public static String guessFieldType(@NotNull Field field) {
        String fieldName = field.getName();

        // match the the fields name wins
        if(fieldName.endsWith("_at") || fieldName.endsWith("At")) {
            return "datetime";
        } else if(fieldName.endsWith("_id") || fieldName.equals("id") || fieldName.endsWith("Id")) {
            return "integer";
        } else if(fieldName.startsWith("is_") || (fieldName.startsWith("is") && fieldName.length() >= 3 && Character.isUpperCase(fieldName.charAt(2)))) {
            return "boolean";
        } else if(fieldName.startsWith("has_") || (fieldName.startsWith("has_") && fieldName.length() >= 4 && Character.isUpperCase(fieldName.charAt(3)))) {
            return "boolean";
        }

        // match on the types; that supports all possible types from PhpStorm but only use primitive types expect for Datetime
        for (String type : field.getType().filterNull().getTypes()) {
            String typeStrip = StringUtils.stripStart(type, "\\");

            if (PhpType.isPrimitiveType(type)) {
                if ("bool".equals(typeStrip)) {
                    return "boolean";
                }

                if ("int".equals(typeStrip)) {
                    return "integer";
                }

                // array is deprecated
                if ("array".equals(typeStrip)) {
                    return "json";
                }

                return typeStrip;
            }

            typeStrip = typeStrip.toLowerCase();

            // special datetime interfaces first
            if (typeStrip.startsWith("datetimeimmutable")) {
                return "datetime_immutable";
            }

            // all DateTime at its interfaces
            if (typeStrip.startsWith("datetime")) {
                return "datetime";
            }
        }

        return "string";
    }

    public static boolean isOrmColumnProperty(@NotNull Field field) {
        PhpDocComment docComment = field.getDocComment();
        if(docComment != null) {
            PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment);
            if (container != null && (container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Column") != null || container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\JoinColumn") != null)) {
                return true;
            }
        }

        return !field.getAttributes("\\Doctrine\\ORM\\Mapping\\Column").isEmpty()
            || !field.getAttributes("\\Doctrine\\ORM\\Mapping\\JoinColumn").isEmpty();
    }

    @Nullable
    public static PhpDocTagAnnotation getOrmEntityPhpDocBlock(@NotNull PhpClass phpClass) {
        PhpDocComment docComment = phpClass.getDocComment();
        if(docComment == null) {
            return null;
        }

        PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment);
        if(container == null) {
            return null;
        }

        return container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Entity");
    }

    public static String underscore(String camelCasedWord) {
        return org.apache.commons.lang3.StringUtils.capitalize(camelCasedWord).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static boolean isDoctrineOrmInVendor(@NotNull Project project)
    {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            IS_DOCTRINE_PROJECT_CACHE,
            () -> {
                Boolean hasClass = !PhpIndex.getInstance(project).getInterfacesByFQN("Doctrine\\ORM\\Mapping\\Annotation").isEmpty()
                    || !PhpIndex.getInstance(project).getClassesByFQN("Doctrine\\ORM\\Mapping\\Entity").isEmpty();

                return CachedValueProvider.Result.create(hasClass, getModificationTrackerForIndexId(project, PhpClassNameIndex.KEY));
            },
            false
        );
    }

    public static String trimBlackSlashes(@NotNull String namespaceName) {

        if(namespaceName.startsWith("\\")) {
            namespaceName = namespaceName.substring(1);
        }

        if(namespaceName.endsWith("\\")) {
            namespaceName = namespaceName.substring(0, namespaceName.length() - 1);
        }

        return namespaceName;
    }

    public static void visitCustomTypes(@NotNull Project project, @NotNull ColumnTypeVisitor visitor) {
        Set<String> found = new HashSet<>();

        Collection<PhpClass> phpClasses = new ArrayList<>();
        PhpIndex.getInstance(project).processAllSubclasses("\\Doctrine\\DBAL\\Types\\Type", phpClass -> {
            phpClasses.add(phpClass);
            return true;
        });

        for (PhpClass phpClass : phpClasses) {
            String name = PhpElementsUtil.getMethodReturnAsString(phpClass, "getName");
            if(name != null) {
                found.add(name);
                visitor.visit(name, phpClass, phpClass.findMethodByName("getName"));
            }
        }

        Stream.of("id", "string", "integer", "smallint", "bigint", "boolean", "decimal", "date", "time", "datetime", "text", "array", "float")
            .filter(s -> !found.contains(s))
            .forEach(s -> visitor.visit(s, null, null));
    }

    public interface ColumnTypeVisitor {
        void visit(@NotNull String name, @Nullable PhpClass phpClass, @Nullable PsiElement psiElement);
    }

    @NotNull
    public static Collection<LookupElement> getTypes(@NotNull Project project) {
        final Collection<LookupElement> lookupElements = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : getColumnTypes(project).entrySet()) {
            LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(entry.getKey()).withIcon(PhpAnnotationIcons.DOCTRINE);

            for (String clazz : entry.getValue()) {
                PhpClass phpClass = PhpElementsUtil.getClassInterface(project, clazz);
                if (phpClass != null) {
                    lookupElementBuilder = lookupElementBuilder.withTypeText(phpClass.getName(), true);

                    if (phpClass.isDeprecated()) {
                        lookupElementBuilder = lookupElementBuilder.withStrikeoutness(true);
                    }
                }
            }

            lookupElements.add(lookupElementBuilder);
        }

        return lookupElements;
    }

    @NotNull
    public static Collection<PhpClass> getColumnTypesTargets(@NotNull Project project, final @NotNull String contents) {
        Set<String> strings = getColumnTypes(project).get(contents);
        if (strings == null) {
            return Collections.emptyList();
        }

        return strings.stream()
            .map(clazz -> PhpElementsUtil.getClassInterface(project, clazz))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @NotNull
    private static Map<String, Set<String>> getColumnTypes(@NotNull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            ANNOTATIONS_TARGETS_CACHE,
            () -> {
                Map<String, Set<String>> items = new HashMap<>();

                visitCustomTypes(project, (name, phpClass, psiElement) -> {
                    if (!items.containsKey(name)) {
                        items.put(name, Collections.emptySet());
                    }

                    if (phpClass != null) {
                        Set<String> current = new HashSet<>(items.get(name));
                        current.add(phpClass.getFQN());

                        items.put(name, Collections.unmodifiableSet(current));
                    }
                });

                // PhpClassNameIndex

                return CachedValueProvider.Result.create(Collections.unmodifiableMap(items), PsiModificationTracker.MODIFICATION_COUNT);
            },
            false
        );
    }

    public static void importOrmUseAliasIfNotExists(@NotNull PhpClassMember field) {

        // check for already imported class aliases
        String qualifiedName = PhpDocUtil.getQualifiedName(field, DOCTRINE_ORM_MAPPING);
        if(qualifiedName == null || !qualifiedName.equals(DOCTRINE_ORM_MAPPING.substring(1))) {
            return;
        }

        // try to import:
        // use Doctrine\ORM\Mapping as ORM;
        PhpClass phpClass = field.getContainingClass();
        if(phpClass == null) {
            return;
        }

        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(phpClass);
        if(scopeForUseOperator == null) {
            return;
        }

        PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, DOCTRINE_ORM_MAPPING, "ORM");
    }

    public static boolean hasCreateRepositoryClassSupport(@NotNull PhpClass phpClass)
    {
        PhpDocComment docComment = phpClass.getDocComment();
        if(docComment != null) {
            PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment);
            if(container != null) {
                PhpDocTagAnnotation phpDocBlock = container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Entity");

                if (phpDocBlock != null) {
                    return AnnotationUtil.getPropertyValueAsPsiElement(phpDocBlock.getPhpDocTag(), "repositoryClass") == null;
                }
            }
        }

        Collection<@NotNull PhpAttribute> attributes = phpClass.getAttributes("\\Doctrine\\ORM\\Mapping\\Entity");
        if (attributes.isEmpty()) {
            return false;
        }

        return attributes
            .stream()
            .flatMap(attribute -> attribute.getArguments().stream())
            .noneMatch(argument -> "repositoryClass".equals(argument.getName()));
    }

    public static boolean repositoryClassExists(PhpDocTag phpDocTag)
    {
        StringLiteralExpression repositoryClass = AnnotationUtil.getPropertyValueAsPsiElement(phpDocTag, "repositoryClass");
        if(repositoryClass == null || StringUtils.isBlank(repositoryClass.getContents())) {
            return false;
        }

        PhpClass phpClass = PhpElementsUtil.getClassInsideAnnotation(repositoryClass, repositoryClass.getContents());
        if(phpClass != null) {
            return true;
        }

        PhpDocComment phpDocComment = PsiTreeUtil.getParentOfType(repositoryClass, PhpDocComment.class);
        if(phpDocComment == null) {
            return false;
        }

        PhpPsiElement phpClassContext = phpDocComment.getNextPsiSibling();
        if(!(phpClassContext instanceof PhpClass)) {
            return false;
        }

        String ns = ((PhpClass) phpClassContext).getNamespaceName();
        String repoClass = repositoryClass.getContents();
        String targetClass;

        if(repoClass.startsWith("\\") || repoClass.startsWith(ns)) {
            targetClass = repoClass;
        } else {
            targetClass = ns + repoClass;
        }

        String classPath = PhpLangUtil.toPresentableFQN(targetClass);
        PhpClass repoPhpClass = PhpElementsUtil.getClass(phpClassContext.getProject(), classPath);

        return repoPhpClass != null;
    }

    private static ModificationTracker getModificationTrackerForIndexId(@NotNull Project project, @NotNull final ID<?, ?> id) {
        return () -> FileBasedIndex.getInstance().getIndexModificationStamp(id, project);
    }
}
