package de.espend.idea.php.annotation.doctrine.inspection;

import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.PsiFile;
import de.espend.idea.php.annotation.util.IdeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class CreateEntityRepositoryIntentionAction extends IntentionAndQuickFixAction {
    @NotNull
    private final String fileName;

    @NotNull
    private final Map<String, String> templateVars;

    @NotNull
    private final String targetPathRelative;

    CreateEntityRepositoryIntentionAction(@NotNull String targetPathRelative, @NotNull String fileName, @NotNull Map<String, String> templateVars) {
        this.targetPathRelative = targetPathRelative;
        this.fileName = fileName;
        this.templateVars = templateVars;
    }

    @NotNull
    @Override
    public String getName() {
        return "Create Doctrine Repository Class";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Annotation";
    }

    @Override
    public void applyFix(@NotNull Project project, PsiFile psiFile, @Nullable Editor editor) {
        String content = createEntityRepositoryContent(templateVars);
        if(content == null) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(
            IdeUtil.createRunnableCreateAndOpenFile(
            project,
            this.targetPathRelative,
            fileName,
            content
        )));
    }

    @Nullable
    public static String createEntityRepositoryContent(Map<String, String> templateVars) {
        String content;

        try {
            content = StreamUtil.readText(CreateEntityRepositoryIntentionAction.class.getResourceAsStream("template/EntityRepository.php"), "UTF-8");
        } catch (IOException e) {
            return null;
        }

        for(Map.Entry<String, String> templateVar: templateVars.entrySet()) {
            content = content.replace("{{" + templateVar.getKey() +"}}", templateVar.getValue());
        }

        return content;
    }
}
