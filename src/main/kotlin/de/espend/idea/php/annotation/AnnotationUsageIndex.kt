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
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpDocTagAnnotationVisitorUtil
import java.io.DataInput
import java.io.DataOutput

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationUsageIndex : FileBasedIndexExtension<String, Set<String>>() {
    private val myKeyDescriptor: KeyDescriptor<String> = EnumeratorStringDescriptor()

    override fun getName(): ID<String, Set<String>> = KEY

    override fun getIndexer(): DataIndexer<String, Set<String>, FileContent> = DataIndexer { inputData ->
        val map = HashMap<String, Set<String>>()
        val psiFile = inputData.psiFile
        if (psiFile !is PhpFile || !AnnotationUtil.isValidForIndex(inputData)) {
            return@DataIndexer map
        }

        PhpDocTagAnnotationVisitorUtil.visitElement(psiFile) { pair ->
            map[pair.first] = hashSetOf()
            true
        }

        map
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = myKeyDescriptor

    override fun getValueExternalizer(): DataExternalizer<Set<String>> = EXTERNALIZER

    override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter { virtualFile ->
        virtualFile.fileType == PhpFileType.INSTANCE
    }

    override fun dependsOnFileContent(): Boolean = true

    override fun getVersion(): Int = 1

    private class StringSetDataExternalizer : DataExternalizer<Set<String>> {
        @Synchronized
        override fun save(out: DataOutput, value: Set<String>) {
            out.writeInt(value.size)
            for (item in value) {
                EnumeratorStringDescriptor.INSTANCE.save(out, item)
            }
        }

        @Synchronized
        override fun read(input: DataInput): Set<String> {
            val set = HashSet<String>()
            repeat(input.readInt()) {
                set.add(EnumeratorStringDescriptor.INSTANCE.read(input))
            }

            return set
        }
    }

    // KEY must remain a Java-accessible static field for AnnotationUtil.
    @Suppress("CompanionObjectInExtension")
    companion object {
        @JvmField
        val KEY: ID<String, Set<String>> = ID.create("espend.php.annotation.usage")

        private val EXTERNALIZER: DataExternalizer<Set<String>> = StringSetDataExternalizer()
    }
}
