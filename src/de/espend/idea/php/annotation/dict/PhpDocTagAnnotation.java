package de.espend.idea.php.annotation.dict;

import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.Nullable;

public class PhpDocTagAnnotation {

    final private PhpClass phpClass;
    final private PhpDocTag phpDocTag;

    public PhpDocTagAnnotation(PhpClass phpClass, PhpDocTag phpDocTag) {
        this.phpClass = phpClass;
        this.phpDocTag = phpDocTag;
    }

    public PhpClass getPhpClass() {
        return phpClass;
    }

    public PhpDocTag getPhpDocTag() {
        return phpDocTag;
    }

    @Nullable
    public String getPropertyValue(String propertyName) {
        PhpPsiElement docAttrList = phpDocTag.getFirstPsiChild();
        if(docAttrList != null) {
            StringLiteralExpression literalExpression = PhpElementsUtil.getChildrenOnPatternMatch(docAttrList, AnnotationPattern.getPropertyIdentifierValue(propertyName));
            if(literalExpression != null) {
                return literalExpression.getContents();
            }
        }

        return null;
    }

}
