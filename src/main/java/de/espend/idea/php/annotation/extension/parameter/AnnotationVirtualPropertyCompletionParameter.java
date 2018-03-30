package de.espend.idea.php.annotation.extension.parameter;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationVirtualPropertyCompletionParameter {
    @NotNull
    private final PhpClass phpClass;

    @NotNull
    final private Map<String, AnnotationPropertyEnum> lookupElements = new HashMap<>();

    public AnnotationVirtualPropertyCompletionParameter(@NotNull PhpClass phpClass) {
        this.phpClass = phpClass;
    }

    public void addLookupElement(@NotNull String element, @NotNull AnnotationPropertyEnum propertyEnum) {
        lookupElements.put(element, propertyEnum);
    }

    @NotNull
    public Map<String, AnnotationPropertyEnum> getLookupElements() {
        return lookupElements;
    }

    @NotNull
    public PhpClass getPhpClass() {
        return phpClass;
    }
}
