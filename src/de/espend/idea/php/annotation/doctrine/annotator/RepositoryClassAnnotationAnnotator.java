package de.espend.idea.php.annotation.doctrine.annotator;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter;
import de.espend.idea.php.annotation.util.IdeUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RepositoryClassAnnotationAnnotator implements PhpAnnotationDocTagAnnotator {

    @Override
    public void annotate(PhpAnnotationDocTagAnnotatorParameter parameter) {

        if(!DoctrineUtil.isDoctrineOrmInVendor(parameter.getProject())) {
            return;
        }

        PhpClass annotationClass = parameter.getAnnotationClass();
        if(annotationClass == null) {
            return;
        }

        if(!"Doctrine\\ORM\\Mapping\\Entity".equals(annotationClass.getPresentableFQN())) {
            return;
        }

        PhpDocTagAnnotation annotationDocTag = parameter.getAnnotationDocTag();
        if(annotationDocTag == null) {
            return;
        }

        StringLiteralExpression repositoryClass = annotationDocTag.getPropertyValuePsi("repositoryClass");
        if(repositoryClass == null) {
            return;
        }

        PhpClass phpClass = PhpElementsUtil.getClassInsideAnnotation(repositoryClass, repositoryClass.getContents());
        if(phpClass != null) {
            return;
        }

        PhpDocComment phpDocComment = PsiTreeUtil.getParentOfType(repositoryClass, PhpDocComment.class);
        if(phpDocComment == null) {
            return;
        }

        PhpPsiElement phpClassContext = phpDocComment.getNextPsiSibling();
        if(!(phpClassContext instanceof PhpClass)) {
            return;
        }

        String ns = ((PhpClass) phpClassContext).getNamespaceName();
        if(ns.startsWith("\\")) {
            ns = ns.substring(1);
        }

        String repoClass = repositoryClass.getContents();
        if(repoClass.startsWith("\\")) {
            return;
        }

        String targetClass;
        if(repoClass.startsWith(ns)) {
            targetClass = repoClass;
        } else {
            targetClass = ns + repoClass;
        }

        String targetClassName = targetClass.substring(targetClass.lastIndexOf("\\") + 1);
        String filename = targetClassName  + ".php";

        Map<String, String> templateVars = new HashMap<String, String>();
        templateVars.put("namespace", ns.endsWith("\\") ? ns.substring(0, ns.length() - 1) : ns);
        templateVars.put("class", targetClassName);

        PsiDirectory directory = phpClassContext.getContainingFile().getContainingDirectory();

        parameter.getHolder().createWarningAnnotation(repositoryClass.getTextRange(), "Create Doctrine repository class")
            .registerFix(new CreateEntityRepositoryIntentionAction(directory, filename, templateVars));

    }

    private static class CreateEntityRepositoryIntentionAction implements IntentionAction {

        private final PsiDirectory psiDirectory;
        private final String fileName;
        private final Map<String, String> templateVars;

        public CreateEntityRepositoryIntentionAction(PsiDirectory psiDirectory, String fileName, Map<String, String> templateVars) {
            this.psiDirectory = psiDirectory;
            this.fileName = fileName;
            this.templateVars = templateVars;
        }

        @NotNull
        @Override
        public String getText() {
            return "Create Doctrine repository class";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "PhpAnnotations";
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
            return true;
        }

        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {

                    IdeUtil.RunnableCreateAndOpenFile runnableCreateAndOpenFile = IdeUtil.getRunnableCreateAndOpenFile(project, psiDirectory, fileName);

                    String content = createEntityRepositoryContent(templateVars);
                    runnableCreateAndOpenFile.setContent(content);

                    ApplicationManager.getApplication().runWriteAction(runnableCreateAndOpenFile);
                }
            });
        }

        @Override
        public boolean startInWriteAction() {
            return true;
        }
    }

    public static String createEntityRepositoryContent(Map<String, String> templateVars) {
        String content = null;

        try {
            content = StreamUtil.readText(RepositoryClassAnnotationAnnotator.class.getResourceAsStream("template/EntityRepository.php"), "UTF-8");
        } catch (IOException e) {
            return null;
        }

        for(Map.Entry<String, String> templateVar: templateVars.entrySet()) {
            content = content.replace("{{" + templateVar.getKey() +"}}", templateVar.getValue());
        }
        return content;
    }
}
