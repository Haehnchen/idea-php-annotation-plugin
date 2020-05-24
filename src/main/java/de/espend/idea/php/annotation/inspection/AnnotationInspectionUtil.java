package de.espend.idea.php.annotation.inspection;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationInspectionUtil {
    @Nullable
    public static String getClassFqnString(@NotNull String namespaceForDocIdentifier, @NotNull Function<Void, Map<String, String>> lazyUseImporterCollector) {
        // absolute class use it directly
        if (namespaceForDocIdentifier.startsWith("\\")) {
            return namespaceForDocIdentifier;
        }

        String[] split = namespaceForDocIdentifier.split("\\\\");
        Map<String, String> useImportMap = lazyUseImporterCollector.apply(null);
        if (useImportMap.containsKey(split[0])) {
            String clazz = useImportMap.get(split[0]);

            // based on the use statement, which can be an alias, attach the doc block class name
            // "@Foobar\TTest" "Foo\Foobar"
            if (split.length > 1) {
                clazz += "\\" + StringUtils.join(Arrays.copyOfRange(split, 1, split.length), "\\");
            }

            return clazz;
        }

        return null;
    }
}
