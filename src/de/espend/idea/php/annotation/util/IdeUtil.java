package de.espend.idea.php.annotation.util;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class IdeUtil {

    public static RunnableCreateAndOpenFile getRunnableCreateAndOpenFile(@NotNull Project project, @NotNull PsiDirectory directory, @NotNull String fileName) {
        return new RunnableCreateAndOpenFile(project, directory, fileName);
    }

    public static class RunnableCreateAndOpenFile implements Runnable {

        private final PsiDirectory directory;
        private final String fileName;
        private final Project project;
        private String content;

        public RunnableCreateAndOpenFile(Project project, PsiDirectory directory, String fileName) {
            this.project = project;
            this.directory = directory;
            this.fileName = fileName;
        }

        public RunnableCreateAndOpenFile setContent(@Nullable String content) {
            this.content = content;
            return this;
        }

        @Override
        public void run() {
            PsiFile virtualFile = createFile(directory, fileName, this.content);
            if(virtualFile != null) {
                new OpenFileDescriptor(project, virtualFile.getVirtualFile(), 0).navigate(true);
            }
        }
    }

    @Nullable
    public static PsiFile createFile(@NotNull PsiDirectory directory, @NotNull String fileNameWithPath, @Nullable String content) {

        PsiFile psiFile = directory.createFile(fileNameWithPath);

        if(content != null) {
            try {
                psiFile.getVirtualFile().setBinaryContent(content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return psiFile;
    }
}
