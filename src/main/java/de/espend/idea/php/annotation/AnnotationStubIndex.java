package de.espend.idea.php.annotation;

import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationStubIndex extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> KEY = ID.create("espend.php.annotation.classes");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new HashMap<>();

            PsiFile psiFile = inputData.getPsiFile();
            if (!(psiFile instanceof PhpFile)) {
                return map;
            }

            if (!AnnotationUtil.isValidForIndex(inputData)) {
                return map;
            }

            for (PhpNamedElement topLevelElement : ((PhpFile) psiFile).getTopLevelDefs().values()) {
                if (topLevelElement instanceof PhpClass phpClass) {
                    String fqn = phpClass.getFQN();
                    if (fqn.startsWith("\\")) {
                        fqn = fqn.substring(1);
                    }

                    // doctrine has many tests: Doctrine\Tests\Common\Annotations\Fixtures
                    // we are on index process, project is not fully loaded here, so filter name based tests
                    // e.g. PhpUnitUtil.isTestClass not possible
                    if (!fqn.contains("\\Tests\\") && !fqn.contains("\\Fixtures\\") && AnnotationUtil.isAnnotationClass(phpClass)) {
                        map.put(fqn, null);
                    }
                }
            }

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
    public DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
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
        return 2;
    }
}
