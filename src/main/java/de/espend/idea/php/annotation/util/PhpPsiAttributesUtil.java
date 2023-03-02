package de.espend.idea.php.annotation.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Helpers for PHP 8 Attributes psi access
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpPsiAttributesUtil {
    @Nullable
    public static String getAttributeValueByNameAsStringWithClassConstant(@NotNull PhpAttribute attribute, @NotNull String attributeName) {
        PsiElement nextSibling = findAttributeByName(attribute, attributeName);

        if (nextSibling instanceof StringLiteralExpression) {
            String contents = ((StringLiteralExpression) nextSibling).getContents();
            if (StringUtils.isNotBlank(contents)) {
                return contents;
            }
        } else if(nextSibling instanceof ClassConstantReference) {
            return resoClassReferenceValue(attribute, (ClassConstantReference) nextSibling);
        }

        return null;
    }

    /**
     * Workaround to find given attribute: "#[Route('/attributesWithoutName', name: "")]" as attribute iteration given the index as "int" but not the key as name
     */
    @Nullable
    private static PsiElement findAttributeByName(@NotNull PhpAttribute attribute, @NotNull String attributeName) {
        ParameterList parameterList = PsiTreeUtil.findChildOfType(attribute, ParameterList.class);
        if (parameterList == null) {
            return null;
        }

        Collection<PsiElement> childrenOfTypeAsList = getChildrenOfTypeAsList(parameterList, getAttributeColonPattern(attributeName));

        if (childrenOfTypeAsList.isEmpty()) {
            return null;
        }

        PsiElement colon = childrenOfTypeAsList.iterator().next();

        return PhpPsiUtil.getNextSibling(colon, psiElement -> psiElement instanceof PsiWhiteSpace);
    }

    /**
     * "#[Route('/path', name: 'attributes_action')]"
     */
    @NotNull
    private static PsiElementPattern.Capture<PsiElement> getAttributeColonPattern(String name) {
        return PlatformPatterns.psiElement().withElementType(
            PhpTokenTypes.opCOLON
        ).afterLeaf(PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER).withText(name));
    }

    @Nullable
    private static String resoClassReferenceValue(@NotNull PhpAttribute attribute, @NotNull ClassConstantReference nextSibling) {
        PhpExpression classReference = nextSibling.getClassReference();
        if (classReference != null) {
            String text = classReference.getText();
            if (StringUtils.isNotBlank(text)) {
                return text;
            }
        }

        return null;
    }

    @NotNull
    private static <T extends PsiElement> Collection<T> getChildrenOfTypeAsList(@Nullable PsiElement element, ElementPattern<T> pattern) {

        Collection<T> collection = new ArrayList<>();

        if (element == null) {
            return collection;
        }

        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (pattern.accepts(child)) {
                //noinspection unchecked
                collection.add((T)child);
            }
        }

        return collection;
    }

    public static void insertNamedArgumentForAttribute(@NotNull Editor editor, @NotNull PhpAttribute phpAttribute, @NotNull String namedArgument, String value) {
        PsiElement lastChild = phpAttribute.getLastChild();
        if (lastChild instanceof ClassReference) {
            editor.getDocument().insertString(lastChild.getTextRange().getEndOffset(), MessageFormat.format("({0}: {1})", namedArgument, value));
        } else {
            String format = MessageFormat.format("{0}: {1}", namedArgument, value);

            if (phpAttribute.getArguments().size() > 0) {
                format = ", " + format;
            }

            editor.getDocument().insertString(lastChild.getTextRange().getStartOffset(), format);
        }
    }
}
