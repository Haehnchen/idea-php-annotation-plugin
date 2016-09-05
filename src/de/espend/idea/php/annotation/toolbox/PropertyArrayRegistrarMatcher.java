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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PropertyArrayRegistrarMatcher implements LanguageRegistrarMatcherInterface {
    @Override
    public boolean matches(@NotNull LanguageMatcherParameter parameter) {
        PsiElement element = parameter.getElement();

        PsiElement docString = element.getParent();
        if(!(docString instanceof StringLiteralExpression)) {
            return false;
        }

        if(!AnnotationPattern.getPropertyArrayPattern().accepts(element)) {
            return false;
        }

        List<JsonSignature> filter = ContainerUtil.filter(parameter.getSignatures(), jsonSignature ->
            "annotation_array".equals(jsonSignature.getType()) &&
            StringUtils.isNotBlank(jsonSignature.getField()) &&
            StringUtils.isNotBlank(jsonSignature.getClassName())
        );

        if(filter.size() == 0) {
            return false;
        }

        PsiElement propertyForEnum = AnnotationUtil.getPropertyForArray((StringLiteralExpression) docString);
        if(propertyForEnum == null) {
            return false;
        }

        String propertyName = propertyForEnum.getText();
        if(StringUtils.isBlank(propertyName)) {
            return false;
        }

        for (JsonSignature signature : filter) {
            if(!propertyName.equals(signature.getField())) {
                continue;
            }

            PhpClass phpClass = AnnotationUtil.getAnnotationReference(PsiTreeUtil.getParentOfType(docString, PhpDocTag.class));
            if(phpClass == null) {
                continue;
            }

            if(StringUtils.stripStart(phpClass.getFQN(), "\\").equalsIgnoreCase(StringUtils.stripStart(signature.getClassName(), "\\"))) {
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
