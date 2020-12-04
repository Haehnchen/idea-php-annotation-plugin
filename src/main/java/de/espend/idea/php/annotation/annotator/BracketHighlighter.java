package de.espend.idea.php.annotation.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import groovy.lang.Tuple2;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BracketHighlighter {
    private static final char[][] BRACKET_PAIRS = {
            {'(', ')'},
            {'{', '}'},
    };

    public static void highlightCaretBracketPair(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder)
    {
        int caretOffset = getCaretOffsetInElement(psiElement);
        String text = psiElement.getText();

        caretOffset = getBracketPositionNextToOffset(caretOffset, text);
        if (caretOffset == -1)
        {
            return;
        }

        List<Tuple2<Integer, Integer>> bracketPairs = findBracketPairs(text);

        annotate(holder, caretOffset + psiElement.getTextOffset());
        annotate(holder, getComplementBracketPosition(caretOffset, bracketPairs) + psiElement.getTextOffset());
    }

    private static void annotate(AnnotationHolder holder, int startPos)
    {
        TextAttributes attr = new TextAttributes(null, null, JBColor.orange, EffectType.BOXED, 0);
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(new TextRange(startPos, startPos + 1))
                .enforcedTextAttributes(attr)
                .create();
    }

    private static List<Tuple2<Integer, Integer>> findBracketPairs(String text)
    {
        List<Tuple2<Integer, Integer>> bracketPairs = new LinkedList<>();

        for(int i = 0, n = text.length() ; i < n ; i++) {
            char c = text.charAt(i);

            if (isOpeningBracketChar(c)) {
                int closingPos = findClosingBracketPosition(i, c, text, bracketPairs);
                if (closingPos == 0)
                    return bracketPairs;

                bracketPairs.add(new Tuple2<>(i, closingPos));

                i = closingPos;
            }
        }

        return bracketPairs;
    }

    private static int findClosingBracketPosition(int openingPos, char openingChar, String text, List<Tuple2<Integer, Integer>> bracketPairs)
    {
        char closingChar = getMatchingClosingBracketChar(openingChar);

        for(int i = openingPos + 1, n = text.length() ; i < n ; i++) {
            char c = text.charAt(i);
            if (isOpeningBracketChar(c)) {
                int closingPos = findClosingBracketPosition(i, c, text, bracketPairs);
                if (closingPos == 0)
                    return 0;

                bracketPairs.add(new Tuple2<>(i, closingPos));

                i = closingPos;

                continue;
            }
            if (c == closingChar) {
                return i;
            }
        }

        return 0;
    }

    private static char getMatchingClosingBracketChar(char openingChar)
    {
        for (char[] ca : BRACKET_PAIRS) {
            if (openingChar == ca[0])
                return ca[1];
        }

        return ' ';
    }

    private static int getComplementBracketPosition(int position, List<Tuple2<Integer, Integer>> bracketPairs)
    {
        for (Tuple2<Integer, Integer> pair : bracketPairs) {
            if (pair.getFirst() == position) return pair.getSecond();
            if (pair.getSecond() == position) return pair.getFirst();
        }

        return 0;
    }

    private static int getCaretOffsetInElement(PsiElement element)
    {
        Editor editor = FileEditorManager.getInstance(element.getProject()).getSelectedTextEditor();

        if (editor == null)
            return -1;

        return editor.getCaretModel().getOffset() - element.getTextRange().getStartOffset();
    }

    public static int getBracketPositionNextToOffset(int offset, String text)
    {
        if (text.length() <= offset || offset < 0) return -1;

        char c = text.charAt(offset);
        if (!isOpeningBracketChar(c) && !isClosingBracketChar(c))
        {
            offset--;
            c = text.charAt(offset);

            if (!isOpeningBracketChar(c) && !isClosingBracketChar(c))
            {
                return -1;
            }
        }

        return offset;
    }

    private static boolean isOpeningBracketChar(char c)
    {
        for (char[] ca : BRACKET_PAIRS) {
            if (c == ca[0])
                return true;
        }

        return false;
    }

    private static boolean isClosingBracketChar(char c)
    {
        for (char[] ca : BRACKET_PAIRS) {
            if (c == ca[1])
                return true;
        }

        return false;
    }
}
