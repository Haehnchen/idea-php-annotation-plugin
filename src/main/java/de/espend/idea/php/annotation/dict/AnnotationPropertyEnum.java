package de.espend.idea.php.annotation.dict;

import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public enum AnnotationPropertyEnum {
    ARRAY, STRING, INTEGER, BOOLEAN;

    public static AnnotationPropertyEnum fromString(@Nullable String value) {
        if (value == null) {
            return STRING;
        }

        if (value.equalsIgnoreCase("string")) {
            return STRING;
        }

        if (value.equalsIgnoreCase("array")) {
            return ARRAY;
        }

        if (value.equalsIgnoreCase("integer") || value.equalsIgnoreCase("int")) {
            return INTEGER;
        }

        if (value.equalsIgnoreCase("boolean") || value.equalsIgnoreCase("bool")) {
            return BOOLEAN;
        }

        // string as fallback as its the most common way; so completion will the wrap cursor with " => name="<caret>"
        return STRING;
    }
}
