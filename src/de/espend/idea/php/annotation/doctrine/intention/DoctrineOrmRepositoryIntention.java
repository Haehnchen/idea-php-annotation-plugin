package de.espend.idea.php.annotation.doctrine.intention;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.annotator.RepositoryClassAnnotationAnnotator;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        PhpClass phpClass = getScopedPhpClass(element);
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

        PhpClass phpClass = getScopedPhpClass(element);
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

            templateVars.put("namespace", DoctrineUtil.trimBlackSlashes(phpClass.getNamespaceName()));
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
            } else {
                if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
                    HintManager.getInstance().showErrorHint(editor, "Repository already exists ");
                }

            }
        }

        PhpDocTagAnnotation ormEntityPhpDocBlock = DoctrineUtil.getOrmEntityPhpDocBlock(phpClass);
        if(ormEntityPhpDocBlock != null) {
            PhpDocTag phpDocTag = ormEntityPhpDocBlock.getPhpDocTag();
            PhpPsiElement firstPsiChild = phpDocTag.getFirstPsiChild();
            insertAttribute(editor, phpClass, phpDocTag, firstPsiChild);
        }

    }

    /**
     * Scope resolve for PhpClass:
     * "@ORM\Entity" or inside PhpClass
     */
    @Nullable
    private PhpClass getScopedPhpClass(PsiElement element) {

        // inside "@ORM\Entity"
        PsiElement parent = element.getParent();
        if(parent instanceof PhpDocTag) {
            PhpDocTagAnnotation phpDocAnnotationContainer = AnnotationUtil.getPhpDocAnnotationContainer((PhpDocTag) parent);

            if(phpDocAnnotationContainer != null) {
                PhpClass phpClass = phpDocAnnotationContainer.getPhpClass();
                if("Doctrine\\ORM\\Mapping\\Entity".equals(phpClass.getPresentableFQN())) {
                    PsiElement docTag = parent.getParent();
                    if(docTag instanceof PhpDocComment) {
                        PhpPsiElement nextPsiSibling = ((PhpDocComment) docTag).getNextPsiSibling();
                        if(nextPsiSibling instanceof PhpClass) {
                            return (PhpClass) nextPsiSibling;
                        }
                    }
                }
            }

            return null;
        }

        // and finally check PhpClass class scope
        return PsiTreeUtil.getParentOfType(element, PhpClass.class);
    }

    private void insertAttribute(@NotNull Editor editor, @NotNull PhpClass phpClass, @NotNull PhpDocTag phpDocTag, @Nullable PhpPsiElement firstPsiChild) {
        if(firstPsiChild == null) return;

        String attr = "repositoryClass=\"" + phpClass.getName() + "Repository" + "\"";

        // we already have an attribute list
        if(firstPsiChild.getNode().getElementType() == PhpDocElementTypes.phpDocAttributeList) {

            if(StringUtils.trim(firstPsiChild.getText()).length() == 0) {
                // @ORM\Entity()
                editor.getDocument().insertString(firstPsiChild.getTextRange().getStartOffset() + 1, attr);
            } else {
                // @ORM\Entity(readOnly=true)
                editor.getDocument().insertString(firstPsiChild.getTextRange().getStartOffset() + 1, attr + ", ");
            }

            return;
        }

        // @ORM\Entity
        PsiElement firstChild = phpDocTag.getFirstChild();
        if(firstChild != null) {
            editor.getDocument().insertString(firstChild.getTextRange().getEndOffset(), "(" + attr + ")");
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
