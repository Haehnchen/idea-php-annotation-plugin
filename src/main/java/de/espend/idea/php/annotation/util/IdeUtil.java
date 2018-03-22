package de.espend.idea.php.annotation.util;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class IdeUtil {
    public static RunnableCreateAndOpenFile createRunnableCreateAndOpenFile(@NotNull Project project, @NotNull String targetPathRelative, @NotNull String fileName, @NotNull String content) {
        return new RunnableCreateAndOpenFile(project, targetPathRelative, fileName, content);
    }

    public static class RunnableCreateAndOpenFile implements Runnable {
        @NotNull
        private final String fileName;

        @NotNull
        private final Project project;

        @NotNull
        private final String targetPathRelative;

        @NotNull
        private String content;

        RunnableCreateAndOpenFile(@NotNull Project project, @NotNull String targetPathRelative, @NotNull String fileName, @NotNull String content) {
            this.project = project;
            this.targetPathRelative = targetPathRelative;
            this.fileName = fileName;
            this.content = content;
        }

        @Override
        public void run() {
            VirtualFile relativeDirectory = VfsUtil.findRelativeFile(targetPathRelative, project.getBaseDir());
            if(relativeDirectory != null) {
                PsiDirectory directory = PsiManager.getInstance(project).findDirectory(relativeDirectory);

                if(directory != null) {
                    PsiFile virtualFile = createFile(directory, fileName, this.content);
                    if(virtualFile != null) {
                        new OpenFileDescriptor(project, virtualFile.getVirtualFile(), 0).navigate(true);
                    }
                }
            }
        }
    }

    @Nullable
    private static PsiFile createFile(@NotNull PsiDirectory directory, @NotNull String fileNameWithPath, @Nullable String content) {
        PsiFile psiFile;

        try {
            psiFile = directory.createFile(fileNameWithPath);
        } catch (IncorrectOperationException e) {
            return null;
        }

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
