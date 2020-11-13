package de.espend.idea.php.annotation.grammar;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class AnnotationError {
    private int startOffset;
    private int stopOffset;
    private final String message;
    private boolean fullError;

    public static AnnotationError fromParseParams(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e)
    {
        int start = 0;
        int stop = 0;
        boolean fullError = true;

        if (o instanceof CommonToken) {
            CommonToken token = ((CommonToken) o);
            start = token.getStartIndex();
            stop = token.getStopIndex();

            fullError = false;
        }
        if (e instanceof LexerNoViableAltException)
        {
            LexerNoViableAltException ex = ((LexerNoViableAltException) e);
            start = ex.getStartIndex();
            stop = ex.getInputStream().size();

            fullError = false;
        }
        return new AnnotationError(start, stop, s, fullError);
    }

    private AnnotationError(int startOffset, int stopOffset, String message, boolean fullError)
    {
        this.startOffset = startOffset;
        this.stopOffset = stopOffset;
        this.message = message;
        this.fullError = fullError;
    }

    public TextRange rangeFromPsiElement(PsiElement element)
    {
        TextRange range = element.getTextRange();

        if (fullError)
            return range;

        if (startOffset > stopOffset) {
            int tmp = startOffset;
            startOffset = stopOffset;
            stopOffset = tmp;
        }

        int start = range.getStartOffset() + startOffset;
        int stop = range.getStartOffset() + stopOffset;
        range = new TextRange(start, stop);

        return range;
    }

    public int getStartOffset()
    {
        return startOffset;
    }

    public int getStopOffset() {
        return stopOffset;
    }

    public String getMessage() {
        return message;
    }
}
