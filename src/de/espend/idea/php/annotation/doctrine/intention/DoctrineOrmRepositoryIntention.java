package de.espend.idea.php.annotation.doctrine.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.annotator.RepositoryClassAnnotationAnnotator;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineOrmRepositoryIntention extends PsiElementBaseIntentionAction {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {

        if(!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        PhpClass phpClass = PsiTreeUtil.getParentOfType(element, PhpClass.class);
        if(phpClass == null) {
            return false;
        }

        PhpDocTagAnnotation ormEntityPhpDocBlock = DoctrineUtil.getOrmEntityPhpDocBlock(phpClass);
        if(ormEntityPhpDocBlock == null) {
            return false;
        }

        if(ormEntityPhpDocBlock.getPropertyValuePsi("repositoryClass") != null) {
            return false;
        }

        return true;

    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {


        PhpClass phpClass = PsiTreeUtil.getParentOfType(element, PhpClass.class);
        if(phpClass == null) {
            return;
        }

        String presentableFQN = phpClass.getPresentableFQN();
        if(presentableFQN == null) {
            return;
        }

        String repoClass = presentableFQN + "Repository";
        PhpClass repoPhpClass = PhpElementsUtil.getClass(project, repoClass);
        if(repoPhpClass == null) {
            Map<String, String> templateVars = new HashMap<String, String>();

            templateVars.put("namespace", phpClass.getNamespaceName());
            templateVars.put("class", phpClass.getName() + "Repository");

            String content = RepositoryClassAnnotationAnnotator.createEntityRepositoryContent(templateVars);


            String fileName = phpClass.getName() + "Repository.php";
            PsiDirectory dir = phpClass.getContainingFile().getContainingDirectory();
            if(dir.findFile(fileName) == null) {
                PsiFile psiFile = dir.createFile(fileName);

                try {
                    psiFile.getVirtualFile().setBinaryContent(content.getBytes());
                } catch (IOException e) {
                    return;
                }
            }
        }

        PhpDocTagAnnotation ormEntityPhpDocBlock = DoctrineUtil.getOrmEntityPhpDocBlock(phpClass);
        if(ormEntityPhpDocBlock != null) {
            PhpDocTag phpDocTag = ormEntityPhpDocBlock.getPhpDocTag();
            PhpPsiElement firstPsiChild = phpDocTag.getFirstPsiChild();
            if(firstPsiChild != null) {
                editor.getDocument().insertString(firstPsiChild.getTextRange().getStartOffset() + 1, "repositoryClass=\"" + phpClass.getName() + "Repository"  + "\"");
            }
        }

    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "PhpAnnotations";
    }

    @NotNull
    @Override
    public String getText() {
        return "Add Doctrine Repository";
    }

}
