package de.espend.idea.php.annotation.lookup;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.jetbrains.php.PhpIcons;
import de.espend.idea.php.annotation.completion.insert.AnnotationPropertyInsertHandler;
import de.espend.idea.php.annotation.dict.AnnotationProperty;
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum;
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
        presentation.setTypeText(this.annotationProperty.getAnnotationPropertyEnum().toString());
        presentation.setTypeGrayed(true);
        if(this.annotationProperty.getAnnotationPropertyEnum() == AnnotationPropertyEnum.ARRAY) {
            presentation.setIcon(PhpIcons.METHOD_ICON);
        } else {
            presentation.setIcon(PhpIcons.FIELD_ICON);
        }

    }

    public void handleInsert(InsertionContext context) {
        AnnotationPropertyInsertHandler.getInstance().handleInsert(context, this);
    }

    @NotNull
    public Object getObject() {
        return this.annotationProperty;
    }
}
