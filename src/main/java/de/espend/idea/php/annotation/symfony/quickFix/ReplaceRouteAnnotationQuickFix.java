package de.espend.idea.php.annotation.symfony.quickFix;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.inspection.quickFix.ReplaceAnnotationWithAnotherQuickFix;
import de.espend.idea.php.annotation.util.AnnotationUtil;

import java.util.ArrayList;
import java.util.List;

public class ReplaceRouteAnnotationQuickFix extends ReplaceAnnotationWithAnotherQuickFix {

    private String[] fieldsToCopy = new String[]{"path", "name", "requirements", "options", "defaults", "host", "methods", "schemes", "condition"};

    public ReplaceRouteAnnotationQuickFix(PhpDocTag phpDocTag, PhpClass deprecatedClass, PhpClass replacementClass) {
        super(phpDocTag, deprecatedClass, replacementClass);
    }

    @Override
    protected void preReplace(Editor editor) {
        PhpDocTag element = (PhpDocTag) myStartElement.getElement();
        if (element == null) {
            return;
        }

        PhpDocTagAnnotation phpDocAnnotationContainer = AnnotationUtil.getPhpDocAnnotationContainer(element);
        if (phpDocAnnotationContainer == null) {
            return;
        }

        List<String> copy = new ArrayList<>();
        String path = phpDocAnnotationContainer.getDefaultPropertyValue();
        if (path != null) {
            copy.add(String.format("%s=\"%s\"", "path", path));
        }

        for (String fieldToCopy : fieldsToCopy) {
            String propertyValue = phpDocAnnotationContainer.getPropertyValue(fieldToCopy);
            if (propertyValue != null) {
                copy.add(String.format("%s=\"%s\"", fieldToCopy, propertyValue));
            }
        }

        StringBuilder sb = new StringBuilder(" @Route(");
        for (String fieldToCopy : copy) {
            sb.append(fieldToCopy);
            if (copy.size() > copy.indexOf(fieldToCopy)) {
                sb.append(", ");
            }
        }

        sb.append(")");

        this.preparedReplacement = sb.toString();
    }

    @Override
    protected void postReplace(Editor editor) {
        if (preparedReplacement == null) {
            return;
        }

        int offset = myStartElement.getElement().getTextOffset();
        myStartElement.getElement().delete();

        Document document = editor.getDocument();
        PsiDocumentManager.getInstance(editor.getProject()).commitDocument(document);
        PsiDocumentManager.getInstance(editor.getProject()).doPostponedOperationsAndUnblockDocument(document);

        document.insertString(offset, preparedReplacement);
        PsiDocumentManager.getInstance(editor.getProject()).commitDocument(document);
    }
}
