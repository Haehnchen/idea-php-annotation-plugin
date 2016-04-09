package de.espend.idea.php.annotation.completion.insert;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil;
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler;
import de.espend.idea.php.annotation.ApplicationSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationTagInsertHandler implements InsertHandler<LookupElement> {

    private static final AnnotationTagInsertHandler instance = new AnnotationTagInsertHandler();

    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {

        // reuse jetbrains "use importer": this is private only so we need some workaround
        // to not implement your own algo for that
        PhpReferenceInsertHandler.getInstance().handleInsert(context, lookupElement);

        if(ApplicationSettings.getInstance().appendRoundBracket && !PhpInsertHandlerUtil.isStringAtCaret(context.getEditor(), "(")) {
            PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), "()");
            context.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
        }

        // "@" is not provide by lookupelements element because its remove by auto import so attach it if necessary
        PsiElement element = PsiUtilCore.getElementAtOffset(context.getFile(), context.getStartOffset());
        if(!element.getText().startsWith("@")) {
            StringBuilder textToInsertBuilder = new StringBuilder();
            textToInsertBuilder.append("@");
            context.getDocument().insertString(context.getStartOffset(), textToInsertBuilder);
        }
    }

    public static AnnotationTagInsertHandler getInstance(){
        return instance;
    }

}