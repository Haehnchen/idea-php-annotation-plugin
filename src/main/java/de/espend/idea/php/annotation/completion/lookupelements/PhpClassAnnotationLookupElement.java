package de.espend.idea.php.annotation.completion.lookupelements;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpClassAnnotationLookupElement extends LookupElement {

    final private PhpClass phpClass;

    @Nullable
    public UseAliasOption getAlias() {
        return alias;
    }

    @Nullable
    private UseAliasOption alias;

    @Nullable
    private InsertHandler<LookupElement> insertHandler = null;

    private String tailText;
    private String typeText;
    private String lookupString;

    public PhpClassAnnotationLookupElement(PhpClass phpClass) {
        this.phpClass = phpClass;
        this.lookupString = phpClass.getName();
    }

    public PhpClassAnnotationLookupElement(PhpClass phpClass, UseAliasOption alias, String lookupString) {
        this.phpClass = phpClass;
        this.alias = alias;
        this.lookupString = lookupString;
    }

    public PhpClassAnnotationLookupElement withInsertHandler(InsertHandler<LookupElement> insertHandler) {
        this.insertHandler = insertHandler;
        return this;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return this.lookupString;
    }

    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(getLookupString());
        presentation.setTypeText(tailText != null ? tailText : this.phpClass.getPresentableFQN());
        presentation.setIcon(this.phpClass.getIcon());
        presentation.setStrikeout(this.phpClass.isDeprecated());
    }

    public void handleInsert(@NotNull InsertionContext context) {
        if (this.insertHandler != null) {
            this.insertHandler.handleInsert(context, this);
        }
    }

    public PhpClassAnnotationLookupElement withTypeText(String typeText) {
        this.typeText = typeText;
        return this;
    }

    @NotNull
    public Object getObject() {
        return this.phpClass;
    }
}
