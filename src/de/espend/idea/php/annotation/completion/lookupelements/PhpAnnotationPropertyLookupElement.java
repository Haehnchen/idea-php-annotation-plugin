package de.espend.idea.php.annotation.completion.lookupelements;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.jetbrains.php.PhpIcons;
import de.espend.idea.php.annotation.completion.insert.AnnotationPropertyInsertHandler;
import de.espend.idea.php.annotation.dict.AnnotationProperty;
import org.jetbrains.annotations.NotNull;

public class PhpAnnotationPropertyLookupElement extends LookupElement {

    private AnnotationProperty annotationProperty;

    public PhpAnnotationPropertyLookupElement(AnnotationProperty annotationProperty) {
        this.annotationProperty = annotationProperty;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return annotationProperty.getPropertyName();
    }


    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(getLookupString());
        presentation.setTypeText(ucfirst(this.annotationProperty.getAnnotationPropertyEnum().toString()));
        presentation.setTypeGrayed(true);
        presentation.setIcon(PhpIcons.FIELD_ICON);
    }

    public void handleInsert(InsertionContext context) {
        AnnotationPropertyInsertHandler.getInstance().handleInsert(context, this);
    }

    private String ucfirst(String chaine){
        return chaine.substring(0, 1).toUpperCase()+ chaine.substring(1).toLowerCase();
    }

    @NotNull
    public Object getObject() {
        return this.annotationProperty;
    }
}
