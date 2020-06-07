package de.espend.idea.php.annotation;

import com.intellij.ide.IconProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.util.PlatformIcons;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Change file icon for a file with only one class and annotate with "@Annotation"
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationIconProvider extends IconProvider {
    public Icon getIcon(@NotNull PsiElement element, @Iconable.IconFlags int flags) {
        if (element instanceof PhpFile && PhpPsiUtil.findClasses((PhpFile)element, AnnotationUtil::isAnnotationClass).size() == 1) {
            return PlatformIcons.ANNOTATION_TYPE_ICON;
        }

        return null;
    }
}
