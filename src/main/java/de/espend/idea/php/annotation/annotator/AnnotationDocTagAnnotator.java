package de.espend.idea.php.annotation.annotator;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationDocTagAnnotatorParameter;
import de.espend.idea.php.annotation.grammar.AnnotationError;
import de.espend.idea.php.annotation.grammar.ErrorListener;
import de.espend.idea.php.annotation.grammar.recognizer.AnnotationsLexer;
import de.espend.idea.php.annotation.grammar.recognizer.AnnotationsParser;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationDocTagAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {

        if(!(psiElement instanceof PhpDocTag)) {
            return;
        }

        String name = ((PhpDocTag) psiElement).getName();
        if(AnnotationUtil.isBlockedAnnotationTag(name)) {
            return;
        }

        if(!AnnotationUtil.isAnnotationPhpDocTag((PhpDocTag) psiElement)) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference(((PhpDocTag) psiElement));
        if(phpClass == null) {

            PhpAnnotationDocTagAnnotatorParameter parameter = new PhpAnnotationDocTagAnnotatorParameter((PhpDocTag) psiElement, holder);
            for(PhpAnnotationDocTagAnnotator annotator: AnnotationUtil.EP_DOC_TAG_ANNOTATOR.getExtensions()) {
                annotator.annotate(parameter);
            }

            return;
        }

        PhpAnnotationDocTagAnnotatorParameter parameter = new PhpAnnotationDocTagAnnotatorParameter(phpClass, (PhpDocTag) psiElement, holder);
        for(PhpAnnotationDocTagAnnotator annotator: AnnotationUtil.EP_DOC_TAG_ANNOTATOR.getExtensions()) {
            annotator.annotate(parameter);
        }

        // only check root objects
        if (psiElement.getParent().getParent() instanceof PhpDocTag) {
            return;
        }

        // parse
        CharStream in = CharStreams.fromString(psiElement.getText());
        AnnotationsLexer lexer = new AnnotationsLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        AnnotationsParser parser = new AnnotationsParser(tokens);

        parser.removeErrorListeners();
        lexer.removeErrorListeners();

        ErrorListener errorListener = new ErrorListener();
        parser.addErrorListener(errorListener);
        lexer.addErrorListener(errorListener);

        parser.start();

        for (AnnotationError error: errorListener.errors) {
            TextRange range = error.rangeFromPsiElement(psiElement);
            AnnotationBuilder builder = holder.newAnnotation(HighlightSeverity.ERROR, error.getMessage()).range(range);

            builder.create();
        }
    }
}

