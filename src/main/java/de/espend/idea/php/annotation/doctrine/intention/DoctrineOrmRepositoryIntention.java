package de.espend.idea.php.annotation.doctrine.intention;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.HighPriorityAction;
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
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.PhpAnnotationIcons;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpDocUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.annotation.util.PhpPsiAttributesUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineOrmRepositoryIntention extends PsiElementBaseIntentionAction implements LocalQuickFix, Iconable, HighPriorityAction {

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {

        if(!DoctrineUtil.isDoctrineOrmInVendor(project)) {
            return false;
        }

        PhpClass phpClass = getScopedPhpClass(element);
        if(phpClass == null) {
            return false;
        }

        return DoctrineUtil.hasCreateRepositoryClassSupport(phpClass);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PhpClass phpClass = getScopedPhpClass(element);
        if(phpClass == null) {
            return;
        }

        // skip for preview
        PsiDirectory containingDirectory = phpClass.getContainingFile().getContainingDirectory();
        if (containingDirectory == null) {
            return;
        }

        String fqn = phpClass.getFQN();
        String sameNamespace = fqn + "Repository";
        if (PhpElementsUtil.getClass(project, sameNamespace) != null) {
            insertRepositoryClass(editor, element, phpClass, sameNamespace);
            return;
        }

        String[] split = StringUtils.split(StringUtils.stripStart(fqn, "\\"), "\\");

        // Foo\Entity\Foobar => Foo\Repository\FoobarRepository
        if (split.length > 2) {
            String repoNamespace = "\\" + StringUtils.join(Arrays.copyOfRange(split, 0, split.length -2), "\\") + "\\Repository\\" + split[split.length - 1] + "Repository";
            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(editor, element, phpClass, repoNamespace);
                return;
            }
        }

        // Foo\Entity\Foo\Foobar => Foo\Repository\Foo\FoobarRepository
        int i = fqn.lastIndexOf("\\Entity\\");
        if (i > 0) {
            String repoNamespace = new StringBuilder(fqn)
                .replace(i, i + "\\Entity\\".length(), "\\Repository\\")
                .append("Repository")
                .toString();

            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(editor, element, phpClass, repoNamespace);
                return;
            }
        }

        // Foo\Entity\Foobar => Foo\Entity\Repository\FoobarRepository
        int i1 = fqn.lastIndexOf("\\");
        if (i1 > 0) {
            String repoNamespace = new StringBuilder(fqn).insert(i1, "\\Repository") +  "Repository";

            if (PhpElementsUtil.getClass(project, repoNamespace) != null) {
                insertRepositoryClass(editor, element, phpClass, repoNamespace);
                return;
            }
        }

        String fileName = phpClass.getName() + "Repository.php";
        PsiDirectory repositoryDir = null;
        PsiDirectory parentDirectory = containingDirectory.getParentDirectory();
        if (parentDirectory != null) {
            repositoryDir = parentDirectory.findSubdirectory("Repository");
            if (repositoryDir == null) {
                repositoryDir = parentDirectory.createSubdirectory("Repository");
            }
        }

        if (repositoryDir == null) {
            if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
                HintManager.getInstance().showErrorHint(editor, "Repository directory structure can not be created");
            }

            return;
        }

        if (repositoryDir.findFile(fileName) != null) {
            if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
                HintManager.getInstance().showErrorHint(editor, "Repository file already exists");
            }

            return;
        }

        String templateName = PhpElementsUtil.getClass(project, "\\Doctrine\\Bundle\\DoctrineBundle\\Repository\\ServiceEntityRepository") != null
            ? "Doctrine Entity ServiceRepository"
            : "Doctrine Entity Repository";

        final FileTemplate fileTemplate = FileTemplateManager.getInstance(project).getInternalTemplate(templateName);
        final Properties defaultProperties = FileTemplateManager.getInstance(project).getDefaultProperties();

        Properties properties = new Properties(defaultProperties);

        String repoClass = new StringBuilder(fqn).insert(fqn.lastIndexOf("\\"), "\\Repository") +  "Repository";
        properties.setProperty("NAMESPACE", StringUtils.stripStart(repoClass.substring(0, repoClass.lastIndexOf("\\")), "\\"));
        properties.setProperty("NAME", phpClass.getName() + "Repository");

        properties.setProperty("ENTITY_NAMESPACE", DoctrineUtil.trimBlackSlashes(phpClass.getNamespaceName()));
        properties.setProperty("ENTITY_NAME", phpClass.getName());

        PsiElement newElement;
        try {
            newElement = FileTemplateUtil.createFromTemplate(fileTemplate, fileName, properties, repositoryDir);
        } catch (Exception e) {
            return;
        }

        insertRepositoryClass(editor, element, phpClass, repoClass);

        new OpenFileDescriptor(project, newElement.getContainingFile().getVirtualFile(), 0).navigate(true);
    }

    private static void insertRepositoryClass(@NotNull Editor editor, @NotNull PsiElement element, @NotNull PhpClass phpClass, @NotNull String repoClass) {
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(element);
        if (scopeForUseOperator == null) {
            return;
        }

        PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, repoClass, null);
        PsiDocumentManager.getInstance(element.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());

        String phpDocTagName = PhpDocUtil.getQualifiedName(phpClass, repoClass);

        PhpDocTagAnnotation ormEntityPhpDocBlock = DoctrineUtil.getOrmEntityPhpDocBlock(phpClass);
        if (ormEntityPhpDocBlock != null) {
            AnnotationUtil.insertNamedArgumentForAnnotation(
                editor,
                ormEntityPhpDocBlock.getPhpDocTag(),
                "repositoryClass",
                phpDocTagName + "::class"
            );

            return;
        }

        Collection<@NotNull PhpAttribute> attributes = phpClass.getAttributes("\\Doctrine\\ORM\\Mapping\\Entity");
        if (attributes.size() > 0) {
            PhpPsiAttributesUtil.insertNamedArgumentForAttribute(editor, attributes.iterator().next(), "repositoryClass", phpDocTagName + "::class");
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
        return "Add Doctrine repository";
    }

    public Icon getIcon(int flags) {
        return PhpAnnotationIcons.DOCTRINE;
    }
}
