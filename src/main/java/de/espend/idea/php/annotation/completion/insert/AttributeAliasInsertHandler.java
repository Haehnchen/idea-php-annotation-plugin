package de.espend.idea.php.annotation.completion.insert;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil;
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.completion.lookupelements.PhpClassAnnotationLookupElement;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AttributeAliasInsertHandler implements InsertHandler<LookupElement> {

    private static final AttributeAliasInsertHandler instance = new AttributeAliasInsertHandler();

    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        // "ORM\Entity"
        if (lookupElement instanceof PhpClassAnnotationLookupElement lookupElement1 && ((PhpClassAnnotationLookupElement) lookupElement).getAlias() != null) {
            PsiElement element = PsiUtilCore.getElementAtOffset(context.getFile(), context.getStartOffset());
            PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(element);

            PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, "\\" + StringUtils.stripStart(lookupElement1.getAlias().getClassName(), "\\"), lookupElement1.getAlias().getAlias());
            PsiDocumentManager.getInstance(context.getProject()).doPostponedOperationsAndUnblockDocument(context.getDocument());
        } else {

            // find alias in settings "\Foo\Bar as Car" for given PhpClass insertion context
            AnnotationTagInsertHandler.preAliasInsertion(context, lookupElement);

            // reuse jetbrains "use importer": this is private only so we need some workaround
            // to not implement your own algo for that
            PhpReferenceInsertHandler.getInstance().handleInsert(context, lookupElement);
        }

        // force "#[Foo]" => "#[Foo(<caret>)]"
        if(ApplicationSettings.getInstance().getState().appendRoundBracket && !PhpInsertHandlerUtil.isStringAtCaret(context.getEditor(), "(")) {
            PhpInsertHandlerUtil.insertStringAtCaret(context.getEditor(), "()");
            context.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
        }
    }

    public static AttributeAliasInsertHandler getInstance(){
        return instance;
    }
}