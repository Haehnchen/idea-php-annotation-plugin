package de.espend.idea.php.annotation.util;

import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.ID;
import de.espend.idea.php.annotation.AnnotationStubIndex;

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
        };

        for(ID<?,?> id: indexIds) {
            FileBasedIndexImpl.getInstance().requestRebuild(id);
            FileBasedIndexImpl.getInstance().scheduleRebuild(id, new Throwable());
        }
    }
}
