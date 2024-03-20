package de.espend.idea.php.annotation.toolbox;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import de.espend.idea.php.toolbox.dict.matcher.LanguageMatcherParameter;
import de.espend.idea.php.toolbox.extension.LanguageRegistrarMatcherInterface;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PropertyRegistrarMatcher implements LanguageRegistrarMatcherInterface {
    @Override
    public boolean matches(@NotNull LanguageMatcherParameter parameter) {

        List<JsonSignature> filter = ContainerUtil.filter(parameter.getSignatures(), jsonSignature -> "annotation".equals(jsonSignature.getType()) &&
        StringUtils.isNotBlank(jsonSignature.getField()) &&
        StringUtils.isNotBlank(jsonSignature.getClassName()));

        if(filter.size() == 0) {
            return false;
        }

        PsiElement phpDocString = parameter.getElement().getParent();
        if(!(phpDocString instanceof StringLiteralExpression)) {
            return false;
        }

        boolean accepts = AnnotationPattern.getTextIdentifier().accepts(parameter.getElement());
        if(!accepts) {
            return false;
        }

        PsiElement propertyName = PhpElementsUtil.getPrevSiblingOfPatternMatch(
            phpDocString,
            PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER)
        );

        if(propertyName == null) {
            return false;
        }

        String fieldName = propertyName.getText();
        if(StringUtils.isBlank(fieldName)) {
            return false;
        }

        for (JsonSignature signature : filter) {
            if(!fieldName.equals(signature.getField())) {
                continue;
            }

            PhpClass phpClass = AnnotationUtil.getAnnotationReference(PsiTreeUtil.getParentOfType(phpDocString, PhpDocTag.class));
            if(phpClass == null) {
                continue;
            }

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
