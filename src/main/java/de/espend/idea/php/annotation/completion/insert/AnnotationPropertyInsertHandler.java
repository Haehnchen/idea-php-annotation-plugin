package de.espend.idea.php.annotation.completion.insert;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil;
import de.espend.idea.php.annotation.dict.AnnotationProperty;
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationPropertyInsertHandler implements InsertHandler<LookupElement> {

    private static final AnnotationPropertyInsertHandler instance = new AnnotationPropertyInsertHandler();

    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {

        // value completion should not fire when already presented:
        // eng| = "value"
        // eng|="value"
        if(PhpInsertHandlerUtil.isStringAtCaret(context.getEditor(), "=") || PhpInsertHandlerUtil.isStringAtCaret(context.getEditor(), " =")) {
           return;
        }

        // move caret back
        int i = -1;

        // append completion text depend on value:
        // engine="|"
        // engine={|}
        // engine=<boolean|integer>
        if(lookupElement.getObject() instanceof AnnotationProperty) {
            String addText = "=\"\"";

            if(((AnnotationProperty) lookupElement.getObject()).getAnnotationPropertyEnum() == AnnotationPropertyEnum.ARRAY) {
                addText = "={}";
            }

            if(((AnnotationProperty) lookupElement.getObject()).getAnnotationPropertyEnum() == AnnotationPropertyEnum.INTEGER || ((AnnotationProperty) lookupElement.getObject()).getAnnotationPropertyEnum() == AnnotationPropertyEnum.BOOLEAN) {
                addText = "=";
                i = 0;
            }

            PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), addText);


        } else {
            PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), "=\"\"");
        }

        if(i != 0) {
            context.getEditor().getCaretModel().moveCaretRelatively(i, 0, false, false, true);
        }

    }

    public static AnnotationPropertyInsertHandler getInstance(){
        return instance;
    }

}
