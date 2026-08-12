package de.espend.idea.php.annotation

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.util.AnnotationUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationStubIndex : FileBasedIndexExtension<String, String>() {
    private val myKeyDescriptor: KeyDescriptor<String> = EnumeratorStringDescriptor()

    override fun getName(): ID<String, String> = KEY

    override fun getIndexer(): DataIndexer<String, String, FileContent> = DataIndexer { inputData ->
        val map = HashMap<String, String>()
        val psiFile = inputData.psiFile
        if (psiFile !is PhpFile || !AnnotationUtil.isValidForIndex(inputData)) {
            return@DataIndexer map
        }

        for (topLevelElement in psiFile.topLevelDefs.values()) {
            if (topLevelElement !is PhpClass) {
                continue
            }

            val fqn = topLevelElement.fqn.removePrefix("\\")

            // doctrine has many tests: Doctrine\Tests\Common\Annotations\Fixtures
            // we are on index process, project is not fully loaded here, so filter name based tests
            // e.g. PhpUnitUtil.isTestClass not possible
            if ("\\Tests\\" in fqn || "\\Fixtures\\" in fqn) {
                continue
            }

            val serializedTargets = AnnotationUtil.getSerializedAnnotationTargets(topLevelElement) ?: continue
            map[fqn] = serializedTargets
        }

        map
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = myKeyDescriptor

    override fun getValueExternalizer(): DataExternalizer<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter { virtualFile ->
        virtualFile.fileType == PhpFileType.INSTANCE
    }

    override fun dependsOnFileContent(): Boolean = true

    override fun getVersion(): Int = 3

    @Suppress("CompanionObjectInExtension") // KEY must remain a Java-accessible static field for AnnotationUtil.
    companion object {
        @JvmField
        val KEY: ID<String, String> = ID.create("espend.php.annotation.classes")
    }
}
