package de.espend.idea.php.annotation;

import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocTagAnnotationRecursiveElementWalkingVisitor;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationUsageIndex extends FileBasedIndexExtension<String, Set<String>> {
    public static final ID<String, Set<String>> KEY = ID.create("espend.php.annotation.usage");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
    private static StringSetDataExternalizer EXTERNALIZER = new StringSetDataExternalizer();

    @NotNull
    @Override
    public ID<String, Set<String>> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Set<String>> map = new THashMap<>();

            PsiFile psiFile = inputData.getPsiFile();
            if(!(psiFile instanceof PhpFile)) {
                return map;
            }

            if(!AnnotationUtil.isValidForIndex(inputData)) {
                return map;
            }

            psiFile.accept(new PhpDocTagAnnotationRecursiveElementWalkingVisitor(pair -> {
                map.put(pair.getFirst(), new HashSet<>());
                return true;
            }));

            return map;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<Set<String>> getValueExternalizer() {
        return EXTERNALIZER;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    private static class StringSetDataExternalizer implements DataExternalizer<Set<String>> {
        public synchronized void save(@NotNull DataOutput out, Set<String> value) throws IOException {
            out.writeInt(value.size());
            Iterator var = value.iterator();

            while(var.hasNext()) {
                String s = (String)var.next();
                EnumeratorStringDescriptor.INSTANCE.save(out, s);
            }
        }

        public synchronized Set<String> read(@NotNull DataInput in) throws IOException {
            Set<String> set = new THashSet<>();

            for(int r = in.readInt(); r > 0; --r) {
                set.add(EnumeratorStringDescriptor.INSTANCE.read(in));
            }

            return set;
        }
    }
}
