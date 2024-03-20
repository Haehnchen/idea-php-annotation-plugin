package de.espend.idea.php.annotation.inspection;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationInspectionUtil {
    @Nullable
    public static String getClassFqnString(@NotNull String namespaceForDocIdentifier, @NotNull LazyNamespaceImportResolver lazyNamespaceImportResolver) {
        // absolute class use it directly
        if (namespaceForDocIdentifier.startsWith("\\")) {
            return namespaceForDocIdentifier;
        }

        String[] split = namespaceForDocIdentifier.split("\\\\");
        Map<String, String> useImportMap = lazyNamespaceImportResolver.getImports();
        if (useImportMap.containsKey(split[0])) {
            String clazz = useImportMap.get(split[0]);

            // based on the use statement, which can be an alias, attach the doc block class name
            // "@Foobar\TTest" "Foo\Foobar"
            if (split.length > 1) {
                clazz += "\\" + StringUtils.join(Arrays.copyOfRange(split, 1, split.length), "\\");
            }

            return clazz;
        }

        String apply = lazyNamespaceImportResolver.getNamespace();
        if (apply != null) {
            return "\\" + StringUtils.strip(apply, "\\") + "\\" + namespaceForDocIdentifier;
        }

        return null;
    }

    public static class LazyNamespaceImportResolver {
        @NotNull
        private final PsiElement psiElement;

        @Nullable
        private Map<String, String> imports = null;

        @Nullable
        private String namespace = null;

        private boolean hasNamespace = false;

        public LazyNamespaceImportResolver(@NotNull PsiElement psiElement) {
            this.psiElement = psiElement;
        }

        @NotNull
        public Map<String, String> getImports() {
            if (imports != null) {
                return this.imports;
            }

            return this.imports = AnnotationUtil.getUseImportMap(psiElement);
        }

        @Nullable
        public String getNamespace() {
            if (hasNamespace) {
                return this.namespace;
            }

            PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(psiElement);

            if (scope instanceof PhpNamespace) {
                String namespaceFqn = ((PhpNamespace) scope).getFQN();
                if (PhpLangUtil.isFqn(namespaceFqn) && !PhpLangUtil.isGlobalNamespaceFQN(namespaceFqn)) {
                    hasNamespace = true;
                    return this.namespace = namespaceFqn;
                }
            }

            return this.namespace = null;
        }
    }
}
