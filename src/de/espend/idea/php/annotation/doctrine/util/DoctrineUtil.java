package de.espend.idea.php.annotation.doctrine.util;

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

}
