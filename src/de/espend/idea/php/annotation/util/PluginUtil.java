package de.espend.idea.php.annotation.util;

import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import de.espend.idea.php.annotation.AnnotationStubIndex;
import de.espend.idea.php.annotation.AnnotationUsageIndex;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PluginUtil {
    /**
     * Force reindex of all internal file indexes
     */
    public static void forceReindex() {
        ID<?,?>[] indexIds = new ID<?,?>[] {
            AnnotationStubIndex.KEY,
            AnnotationUsageIndex.KEY,
        };

        for(ID<?,?> id: indexIds) {
            FileBasedIndex.getInstance().requestRebuild(id);
            FileBasedIndex.getInstance().scheduleRebuild(id, new Throwable());
        }
    }
}
