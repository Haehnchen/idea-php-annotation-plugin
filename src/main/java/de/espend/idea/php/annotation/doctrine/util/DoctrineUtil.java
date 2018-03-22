package de.espend.idea.php.annotation.doctrine.util;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.PhpAnnotationIcons;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    public static String guessFieldType(String fieldName) {

        if(fieldName.endsWith("_at") || fieldName.endsWith("At")) {
            return "datetime";
        } else if(fieldName.endsWith("_id") || fieldName.equals("id") || fieldName.endsWith("Id")) {
            return "integer";
        } else if(fieldName.startsWith("is_") || (fieldName.startsWith("is") && fieldName.length() >= 3 && Character.isUpperCase(fieldName.charAt(2)))) {
            return "boolean";
        } else if(fieldName.startsWith("has_") || (fieldName.startsWith("has_") && fieldName.length() >= 4 && Character.isUpperCase(fieldName.charAt(3)))) {
            return "boolean";
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

    public static Collection<LookupElement> getTypes(@NotNull Project project) {

        final Collection<LookupElement> lookupElements = new ArrayList<>();

        visitCustomTypes(project, (name, phpClass, psiElement) -> {
            LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(name).withIcon(PhpAnnotationIcons.DOCTRINE);

            if(phpClass != null) {
                lookupElementBuilder = lookupElementBuilder.withTypeText(phpClass.getName(), true);
            }

            lookupElements.add(lookupElementBuilder);
        });

        return lookupElements;
    }

    public static Collection<PsiElement> getColumnTypesTargets(@NotNull Project project, final @NotNull String contents) {

        final Collection<PsiElement> targets = new ArrayList<>();

        visitCustomTypes(project, (name, phpClass, psiElement) -> {
            if(!name.equals(contents)) {
                return;
            }

            LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(name).withIcon(PhpAnnotationIcons.DOCTRINE);
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
}
