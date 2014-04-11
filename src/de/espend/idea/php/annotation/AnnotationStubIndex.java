package de.espend.idea.php.annotation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpConstantNameIndex;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PluginUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import com.intellij.util.indexing.ScalarIndexExtension;

import java.util.Map;


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
        return new DataIndexer<String, Void, FileContent>() {
            @NotNull
            @Override
            public Map<String, Void> map(FileContent inputData) {
                final Map<String, Void> map = new THashMap<String, Void>();

                if(!(inputData.getPsiFile() instanceof PhpFile)) {
                    return map;
                }

                if(!PluginUtil.isEnabled(inputData.getPsiFile())) {
                    return map;
                }

                inputData.getPsiFile().accept(new PsiRecursiveElementWalkingVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if ((element instanceof PhpClass)) {
                            visitPhpClass((PhpClass)element);
                        }

                        super.visitElement(element);
                    }

                    private void visitPhpClass(PhpClass phpClass) {
                        String fqn = phpClass.getPresentableFQN();
                        if(fqn == null) {
                            return;
                        }

                        // doctrine has many tests: Doctrine\Tests\Common\Annotations\Fixtures
                        // we are on index process, project is not fully loaded here, so filter name based tests
                        // eg PhpUnitUtil.isTestClass not possible
                        if(!fqn.contains("\\Tests\\") && !fqn.contains("\\Fixtures\\") && AnnotationUtil.isAnnotationClass(phpClass)) {
                            map.put(phpClass.getPresentableFQN(), null);
                            return;
                        }

                        // on class change cleanup. right?
                        if(map.containsKey(fqn)) {
                            map.remove(fqn);
                        }

                    }

                });

                return map;
            }
        };
    }

    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @Override
    public DataExternalizer<Void> getValueExternalizer() {
        return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
    }

    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return PhpConstantNameIndex.PHP_INPUT_FILTER;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
