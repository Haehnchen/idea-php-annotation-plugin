package de.espend.idea.php.annotation.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.ID;
import de.espend.idea.php.annotation.AnnotationStubIndex;
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

    public static void forceReindex() {

        ID<?,?>[] indexIds = new ID<?,?>[] {
            AnnotationStubIndex.KEY,
        };

        for(ID<?,?> id: indexIds) {
            FileBasedIndexImpl.getInstance().requestRebuild(id);
            FileBasedIndexImpl.getInstance().scheduleRebuild(id, new Throwable());
        }
    }
}
