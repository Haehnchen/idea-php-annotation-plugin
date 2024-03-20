package de.espend.idea.php.annotation.toolbox;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import de.espend.idea.php.toolbox.dict.matcher.LanguageMatcherParameter;
import de.espend.idea.php.toolbox.extension.LanguageRegistrarMatcherInterface;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DefaultPropertyRegistrarMatcher implements LanguageRegistrarMatcherInterface {
    @Override
    public boolean matches(@NotNull LanguageMatcherParameter parameter) {

        List<JsonSignature> filter = ContainerUtil.filter(parameter.getSignatures(), jsonSignature -> "annotation".equals(jsonSignature.getType()) && StringUtils.isNotBlank(jsonSignature.getClassName()));

        if(filter.isEmpty()) {
            return false;
        }

        PsiElement parent = parameter.getElement().getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return false;
        }

        boolean accepts = AnnotationPattern.getDefaultPropertyValueString().accepts(parent);
        if(!accepts) {
            return false;
        }

        PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(parent, PhpDocTag.class);
        PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
        if(phpClass == null) {
            return false;
        }

        for (JsonSignature signature : filter) {
            if(StringUtils.stripStart(phpClass.getFQN(), "\\").equals(StringUtils.stripStart(signature.getClassName(), "\\"))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean supports(@NotNull FileType fileType) {
        return PhpFileType.INSTANCE == fileType;
    }
}
