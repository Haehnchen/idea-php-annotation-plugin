package de.espend.idea.php.annotation.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import de.espend.idea.php.annotation.Settings;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PluginUtil {

    public static boolean isEnabled(Project project) {
        return true;
    }

    public static boolean isEnabled(@Nullable PsiElement psiElement) {
        return psiElement != null && isEnabled(psiElement.getProject());
    }

}
