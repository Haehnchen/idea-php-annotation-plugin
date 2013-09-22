package de.espend.idea.php.annotation.completion.insert;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil;
import de.espend.idea.php.annotation.dict.AnnotationProperty;
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum;
import org.jetbrains.annotations.NotNull;

public class AnnotationPropertyInsertHandler implements InsertHandler<LookupElement> {

    private static final AnnotationPropertyInsertHandler instance = new AnnotationPropertyInsertHandler();

    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        if(lookupElement.getObject() instanceof AnnotationProperty) {
            String addText = "=\"\"";

            if(((AnnotationProperty) lookupElement.getObject()).getAnnotationPropertyEnum() == AnnotationPropertyEnum.ARRAY) {
                addText = "={}";
            }
            PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), addText);

        } else {
            PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), "=\"\"");
        }

        context.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
    }

    public static AnnotationPropertyInsertHandler getInstance(){
        return instance;
    }

}
