package de.espend.idea.php.annotation.completion.lookupelements;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpClassAnnotationLookupElement extends LookupElement {

    final private PhpClass phpClass;

    @Nullable
    private InsertHandler<LookupElement> insertHandler = null;

    private String tailText;
    private String typeText;

    public PhpClassAnnotationLookupElement(PhpClass phpClass) {
        this.phpClass = phpClass;
    }

    public PhpClassAnnotationLookupElement withInsertHandler(InsertHandler<LookupElement> insertHandler) {
        this.insertHandler = insertHandler;
        return this;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return phpClass.getName();
    }

    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(getLookupString());
        presentation.setTailText(tailText != null ? tailText : this.phpClass.getPresentableFQN(), true);
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

    public PhpClassAnnotationLookupElement withTailText(String tailText) {
        this.tailText = tailText;
        return this;
    }
}
