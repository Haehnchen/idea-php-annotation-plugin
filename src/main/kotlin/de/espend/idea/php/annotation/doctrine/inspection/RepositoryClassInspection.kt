package de.espend.idea.php.annotation.doctrine.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import de.espend.idea.php.annotation.doctrine.intention.DoctrineOrmRepositoryIntention
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil
import de.espend.idea.php.annotation.util.AnnotationUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
open class RepositoryClassInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!DoctrineUtil.isDoctrineOrmInVendor(holder.project)) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : MyAnnotationPropertyPsiElementVisitor("Doctrine\\ORM\\Mapping\\Entity") {
            override fun visitAnnotationProperty(phpDocTag: PhpDocTag) {
                val repositoryClass = AnnotationUtil.getPropertyValueAsPsiElement(
                    phpDocTag,
                    "repositoryClass",
                ) ?: return

                if (!DoctrineUtil.repositoryClassExists(phpDocTag)) {
                    holder.registerProblem(
                        repositoryClass,
                        MESSAGE,
                        DoctrineOrmRepositoryIntention(),
                    )
                }
            }
        }
    }

    private abstract class MyAnnotationPropertyPsiElementVisitor(
        private val className: String,
    ) : PsiElementVisitor() {
        override fun visitElement(psiElement: PsiElement) {
            if (psiElement !is PhpDocTag) {
                super.visitElement(psiElement)
                return
            }

            val name = psiElement.name
            if (AnnotationUtil.isBlockedAnnotationTag(name)) {
                super.visitElement(psiElement)
                return
            }

            if (!AnnotationUtil.isAnnotationPhpDocTag(psiElement)) {
                super.visitElement(psiElement)
                return
            }

            val phpClass = AnnotationUtil.getAnnotationReference(psiElement)
            if (phpClass == null) {
                super.visitElement(psiElement)
                return
            }

            if (className != phpClass.presentableFQN) {
                super.visitElement(psiElement)
                return
            }

            visitAnnotationProperty(psiElement)
            super.visitElement(psiElement)
        }

        protected abstract fun visitAnnotationProperty(phpDocTag: PhpDocTag)
    }

    companion object {
        const val MESSAGE = "[Annotations] Missing repository class"
    }
}
