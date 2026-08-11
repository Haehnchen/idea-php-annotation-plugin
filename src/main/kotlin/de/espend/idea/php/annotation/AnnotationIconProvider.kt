package de.espend.idea.php.annotation

import com.intellij.ide.IconProvider
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Iconable.IconFlags
import com.intellij.psi.PsiElement
import com.intellij.util.PlatformIcons
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.PhpPsiUtil
import de.espend.idea.php.annotation.util.AnnotationUtil
import javax.swing.Icon

/**
 * Change file icon for a file with only one class and annotate with "@Annotation"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, @IconFlags flags: Int): Icon? {
        if (element is PhpFile && PhpPsiUtil.findClasses(
                element,
                Condition { phpClass -> AnnotationUtil.isAnnotationClass(phpClass) }).size == 1
        ) {
            return PlatformIcons.ANNOTATION_TYPE_ICON
        }

        return null
    }
}
