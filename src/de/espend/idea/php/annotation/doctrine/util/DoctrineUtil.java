package de.espend.idea.php.annotation.doctrine.util;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineUtil {

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

    public static boolean isOrmColumnProperty(@NotNull Field field)
    {

        PhpDocComment docComment = field.getDocComment();
        if(docComment == null) {
            return false;
        }

        PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(docComment);
        if(container == null) {
            return false;
        }

        return container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Column") != null;

    }

    public static String underscore(String camelCasedWord) {
        return org.apache.commons.lang.StringUtils.capitalize(camelCasedWord).replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static boolean isDoctrineOrmInVendor(@NotNull Project project)
    {
        // backward compatibility
        return
            PhpIndex.getInstance(project).getInterfacesByFQN("\\Doctrine\\ORM\\Mapping\\Annotation").size() > 0 ||
            PhpIndex.getInstance(project).getInterfacesByFQN("Doctrine\\ORM\\Mapping\\Annotation").size() > 0;
    }

}
