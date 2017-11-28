package de.espend.idea.php.annotation.extension;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface PhpAnnotationUseAlias {
    /**
     * Provide custom aliases for annotations eg: "ORM" => "Doctrine\\ORM\\Mapping"
     */
    @NotNull
    Map<String, String> getAliases();
}
