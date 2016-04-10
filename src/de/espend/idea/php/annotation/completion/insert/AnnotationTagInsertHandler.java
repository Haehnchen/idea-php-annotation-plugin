package de.espend.idea.php.annotation.completion.insert;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.completion.insert.PhpInsertHandlerUtil;
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationTagInsertHandler implements InsertHandler<LookupElement> {

    private static final AnnotationTagInsertHandler instance = new AnnotationTagInsertHandler();

    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {

        // find alias in settings "\Foo\Bar as Car" for given PhpClass insertion context
        preAliasInsertion(context, lookupElement);

        // reuse jetbrains "use importer": this is private only so we need some workaround
        // to not implement your own algo for that
        PhpReferenceInsertHandler.getInstance().handleInsert(context, lookupElement);

        // force "@Foo" => "@Foo(<caret>)"
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

    /**
     * Insert class alias before PhpStorm tries to import a new use statement "\Foo\Bar as Car"
     */
    private void preAliasInsertion(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        List<UseAliasOption> importsAliases = getImportsAliases();
        if(importsAliases.size() == 0) {
            return;
        }

        Object object = lookupElement.getObject();
        if(!(object instanceof PhpClass)) {
            return;
        }

        final String fqn = StringUtils.stripStart(((PhpClass) object).getFQN(), "\\");

        UseAliasOption useAliasOption = ContainerUtil.find(ApplicationSettings.getInstance().useAliasOptions, new Condition<UseAliasOption>() {
            @Override
            public boolean value(UseAliasOption useAliasOption) {
                return useAliasOption.getAlias() != null &&
                    useAliasOption.getClassName() != null &&
                    fqn.startsWith(StringUtils.stripStart(useAliasOption.getClassName(), "\\"))
                ;
            }
        });

        if(useAliasOption == null || useAliasOption.getClassName() == null || useAliasOption.getAlias() == null) {
            return;
        }

        PsiElement elementAt = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
        if(elementAt == null) {
            return;
        }


        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(elementAt);
        if(scopeForUseOperator == null) {
            return;
        }

        String className = useAliasOption.getClassName();
        if(!className.startsWith("\\")) {
            className = "\\" + className;
        }

        PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, className, useAliasOption.getAlias());
        PsiDocumentManager.getInstance(context.getProject()).doPostponedOperationsAndUnblockDocument(context.getDocument());
    }

    private List<UseAliasOption> getImportsAliases() {
        if(ApplicationSettings.getInstance().useAliasOptions == null || ApplicationSettings.getInstance().useAliasOptions.size() == 0) {
            return Collections.emptyList();
        }

        return ContainerUtil.filter(ApplicationSettings.getInstance().useAliasOptions, new Condition<UseAliasOption>() {
            @Override
            public boolean value(UseAliasOption option) {
                return option.isEnabled();
            }
        });
    }

    public static AnnotationTagInsertHandler getInstance(){
        return instance;
    }

}