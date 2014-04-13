package de.espend.idea.php.annotation.completion.lookupelements;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhpClassAnnotationLookupElement extends LookupElement {

    private PhpClass phpClass;

    @Nullable
    private InsertHandler<LookupElement> insertHandler = null;

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
        presentation.setTypeText(this.phpClass.getPresentableFQN());
        presentation.setTypeGrayed(true);
        presentation.setIcon(PhpIcons.CLASS);
    }

    public void handleInsert(InsertionContext context) {
        if (this.insertHandler != null) {
            this.insertHandler.handleInsert(context, this);
        }
    }

    public Object getObject() {
        return this.phpClass;
    }

}
