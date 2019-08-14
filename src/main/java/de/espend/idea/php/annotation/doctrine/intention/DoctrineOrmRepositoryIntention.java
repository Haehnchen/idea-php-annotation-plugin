package de.espend.idea.php.annotation.doctrine.intention;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineOrmRepositoryIntention extends PsiElementBaseIntentionAction implements LocalQuickFix {

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

        return ormEntityPhpDocBlock.getPropertyValuePsi("repositoryClass") == null;

    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

        PhpClass phpClass = getScopedPhpClass(element);
        if(phpClass == null) {
            return;
        }

        String repoClass = phpClass.getPresentableFQN() + "Repository";
        PhpClass repoPhpClass = PhpElementsUtil.getClass(project, repoClass);
        if(repoPhpClass == null) {
            String fileName = phpClass.getName() + "Repository.php";
            PsiDirectory dir = phpClass.getContainingFile().getContainingDirectory();
            if(dir.findFile(fileName) == null) {
                final FileTemplate fileTemplate = FileTemplateManager.getInstance(project).getCodeTemplate("Doctrine Entity Repository");
                final Properties defaultProperties = FileTemplateManager.getInstance(project).getDefaultProperties();

                Properties properties = new Properties(defaultProperties);

                properties.setProperty("NAMESPACE", DoctrineUtil.trimBlackSlashes(phpClass.getNamespaceName()));
                properties.setProperty(FileTemplate.ATTRIBUTE_NAME, phpClass.getName() + "Repository");

                try {
                    PsiElement newElement = FileTemplateUtil.createFromTemplate(fileTemplate, fileName, properties, dir);

                    new OpenFileDescriptor(project, newElement.getContainingFile().getVirtualFile(), 0).navigate(true);
                } catch (Exception e) {
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

        // inside "@ORM\Entity(<caret>)"
        if(parent.getNode().getElementType() == PhpDocElementTypes.phpDocAttributeList) {
            parent = parent.getParent();
        }

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

            // AttributeList: "()", "(aaa)"
            String text = StringUtils.trim(firstPsiChild.getText());
            text = StringUtils.strip(text, "(");
            text = StringUtils.strip(text, ")");

            if(text.length() == 0) {
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

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName()
    {
        return getText();
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "PhpAnnotations";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor)
    {
        if (descriptor.getPsiElement() == null) return;
        if (descriptor.getPsiElement().getContainingFile() == null) return;

        invoke(project, null, descriptor.getPsiElement().getContainingFile());
    }

    @NotNull
    @Override
    public String getText() {
        return "Add doctrine repository";
    }
}
