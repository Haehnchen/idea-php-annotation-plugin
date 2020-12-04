package de.espend.idea.php.annotation.annotator;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import de.espend.idea.php.annotation.ApplicationSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class AnnotationCaretListener implements StartupActivity, DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        final EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        if (ApplicationSettings.getInstance().activateBracketHighlighting) {
            eventMulticaster.addCaretListener(new CaretListener() {
                @Override
                public void caretPositionChanged(@NotNull CaretEvent e) {
                    final PsiFile psiFile = psiDocumentManager.getPsiFile(e.getEditor().getDocument());
                    if (psiFile instanceof PhpFile) {
                        int offset = e.getEditor().logicalPositionToOffset(e.getNewPosition());
                        if (BracketHighlighter.getBracketPositionNextToOffset(offset, psiFile.getText()) == -1)
                            return;

                        FileContentUtil.reparseFiles(project, Collections.singletonList(psiFile.getVirtualFile()), true);
                    }
                }
            }, project);
        }
    }
}