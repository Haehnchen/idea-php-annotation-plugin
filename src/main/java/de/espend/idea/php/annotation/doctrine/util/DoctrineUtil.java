package de.espend.idea.php.annotation.doctrine.util;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import de.espend.idea.php.annotation.PhpAnnotationIcons;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
        if(docComment == null) {
            return false;
        }

        PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment);
        return container != null
            && (container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Column") != null || container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\JoinColumn") != null);
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
        return org.apache.commons.lang.StringUtils.capitalize(camelCasedWord).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static boolean isDoctrineOrmInVendor(@NotNull Project project)
    {
        return
            PhpIndex.getInstance(project).getInterfacesByFQN("Doctrine\\ORM\\Mapping\\Annotation").size() > 0;
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

        for (PhpClass phpClass : PhpIndex.getInstance(project).getAllSubclasses("\\Doctrine\\DBAL\\Types\\Type")) {
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

        visitCustomTypes(project, (name, phpClass, psiElement) -> {
            LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(name).withIcon(PhpAnnotationIcons.DOCTRINE);

            if(phpClass != null) {
                lookupElementBuilder = lookupElementBuilder.withTypeText(phpClass.getName(), true);

                if (phpClass.isDeprecated()) {
                    lookupElementBuilder = lookupElementBuilder.withStrikeoutness(true);
                }
            }

            lookupElements.add(lookupElementBuilder);
        });

        return lookupElements;
    }

    @NotNull
    public static Collection<PhpClass> getColumnTypesTargets(@NotNull Project project, final @NotNull String contents) {
        final Collection<PhpClass> targets = new ArrayList<>();

        visitCustomTypes(project, (name, phpClass, psiElement) -> {
            if(!name.equals(contents)) {
                return;
            }

            if(phpClass != null) {
                targets.add(phpClass);
            }
        });

        return targets;
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
}
