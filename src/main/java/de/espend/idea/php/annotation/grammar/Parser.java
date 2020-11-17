package de.espend.idea.php.annotation.grammar;

import de.espend.idea.php.annotation.grammar.recognizer.AnnotationsLexer;
import de.espend.idea.php.annotation.grammar.recognizer.AnnotationsParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;

public class Parser {
    public List<AnnotationError> parse(String text) {
        CharStream in = CharStreams.fromString(text);
        AnnotationsLexer lexer = new AnnotationsLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        AnnotationsParser parser = new AnnotationsParser(tokens);

        parser.removeErrorListeners();
        lexer.removeErrorListeners();

        ErrorListener errorListener = new ErrorListener();
        parser.addErrorListener(errorListener);
        lexer.addErrorListener(errorListener);

        parser.start();

        return errorListener.errors;
    }
}
