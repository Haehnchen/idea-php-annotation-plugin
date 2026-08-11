package de.espend.idea.php.annotation.util

import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import de.espend.idea.php.annotation.AnnotationStubIndex
import de.espend.idea.php.annotation.AnnotationUsageIndex

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class PluginUtil private constructor() {
    companion object {
        /** Force a rebuild of all plugin-owned file indexes. */
        fun forceReindex() {
            val indexIds: Array<ID<*, *>> = arrayOf(AnnotationStubIndex.KEY, AnnotationUsageIndex.KEY)
            for (id in indexIds) {
                FileBasedIndex.getInstance().requestRebuild(id)
            }
        }
    }
}
