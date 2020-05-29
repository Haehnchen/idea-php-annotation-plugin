package de.espend.idea.php.annotation.dict;

import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpDocTagAnnotation {

    final private PhpClass phpClass;
    final private PhpDocTag phpDocTag;

    public PhpDocTagAnnotation(@NotNull PhpClass phpClass, @NotNull PhpDocTag phpDocTag) {
        this.phpClass = phpClass;
        this.phpDocTag = phpDocTag;
    }

    @NotNull
    public PhpClass getPhpClass() {
        return phpClass;
    }

    @NotNull
    public PhpDocTag getPhpDocTag() {
        return phpDocTag;
    }

    /**
     * Get property Value from "@Template(template="foo");
     *
     * @param propertyName property name template=""
     * @return Property value
     */
    @Nullable
    public String getPropertyValue(@NotNull String propertyName) {
        return AnnotationUtil.getPropertyValue(phpDocTag, propertyName);
    }

    /**
     * Get property psi element
     *
     * @param propertyName property name template=""
     * @return Property value
     */
    @Nullable
    public StringLiteralExpression getPropertyValuePsi(@NotNull String propertyName) {
        PhpPsiElement docAttrList = phpDocTag.getFirstPsiChild();
        if(docAttrList != null) {
            return PhpElementsUtil.getChildrenOnPatternMatch(docAttrList, AnnotationPattern.getPropertyIdentifierValue(propertyName));
        }

        return null;
    }

    /**
     * Get default property value from annotation "@Template("foo");
     *
     * @return Content of property value literal
     */
    @Nullable
    public String getDefaultPropertyValue() {
        return AnnotationUtil.getDefaultPropertyValue(phpDocTag);
    }
}
