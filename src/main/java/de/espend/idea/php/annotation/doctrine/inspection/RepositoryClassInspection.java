package de.espend.idea.php.annotation.doctrine.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RepositoryClassInspection extends LocalInspectionTool {

    public static final String MESSAGE = "Missing repository class";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        if(!DoctrineUtil.isDoctrineOrmInVendor(holder.getProject())) {
            return super.buildVisitor(holder, isOnTheFly);
        }

        return new MyAnnotationPropertyPsiElementVisitor("Doctrine\\ORM\\Mapping\\Entity") {
            @Override
            protected void visitAnnotationProperty(@NotNull PhpDocTag phpDocTag) {
                StringLiteralExpression repositoryClass = AnnotationUtil.getPropertyValueAsPsiElement(phpDocTag, "repositoryClass");
                if(repositoryClass == null || StringUtils.isBlank(repositoryClass.getContents())) {
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

                PsiFile containingFile = phpClassContext.getContainingFile();
                if(containingFile == null) {
                    return;
                }

                PsiDirectory directory = containingFile.getContainingDirectory();
                if(directory == null) {
                    return;
                }

                if(directory.findFile(filename) == null) {
                    String relativePath = VfsUtil.getRelativePath(directory.getVirtualFile(), phpDocTag.getProject().getBaseDir(), '/');

                    // wrong quick fix folder must not break inspection
                    if(relativePath != null) {
                        holder.registerProblem(
                            repositoryClass,
                            MESSAGE,
                            new DoctrineOrmRepositoryIntention()
                        );
                    } else {
                        holder.registerProblem(repositoryClass, MESSAGE);
                    }
                }
            }
        };
    }

    private static abstract class MyAnnotationPropertyPsiElementVisitor extends PsiElementVisitor {

        @NotNull
        private final String className;

        MyAnnotationPropertyPsiElementVisitor(@NotNull String className) {
            this.className = className;
        }

        @Override
        public void visitElement(PsiElement psiElement) {
            if(!(psiElement instanceof PhpDocTag)) {
                super.visitElement(psiElement);
                return;
            }

            String name = ((PhpDocTag) psiElement).getName();
            if(AnnotationUtil.NON_ANNOTATION_TAGS.contains(name)) {
                super.visitElement(psiElement);
                return;
            }

            if(!AnnotationUtil.isAnnotationPhpDocTag((PhpDocTag) psiElement)) {
                super.visitElement(psiElement);
                return;
            }

            PhpClass phpClass = AnnotationUtil.getAnnotationReference(((PhpDocTag) psiElement));
            if(phpClass == null) {
                super.visitElement(psiElement);
                return;
            }

            if(!this.className.equals(phpClass.getPresentableFQN())) {
                super.visitElement(psiElement);
                return;
            }

            visitAnnotationProperty((PhpDocTag) psiElement);

            super.visitElement(psiElement);
        }

        abstract protected void visitAnnotationProperty(@NotNull PhpDocTag phpDocTag);
    }
}
