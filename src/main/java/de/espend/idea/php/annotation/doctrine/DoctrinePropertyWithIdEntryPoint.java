package de.espend.idea.php.annotation.doctrine;

import com.intellij.codeInspection.reference.EntryPoint;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class DoctrinePropertyWithIdEntryPoint extends EntryPoint {
    @Override
    public @NotNull
    @Nls
    String getDisplayName() {
        return "Properties with id annotation";
    }

    @Override
    public boolean isEntryPoint(@NotNull RefElement refElement, @NotNull PsiElement psiElement) {
        return isEntryPoint(psiElement);
    }

    @Override
    public boolean isEntryPoint(@NotNull PsiElement psiElement) {
        if (psiElement instanceof PhpNamedElement) {
            PhpDocComment comment = ((PhpNamedElement) psiElement).getDocComment();
            return comment != null && comment.getTagElementsByName("@ORM\\Id").length > 0;
        }
        return false;

    }

    @Override
    public boolean isSelected() {
        return true;
    }

    @Override
    public void setSelected(boolean selected) {

    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {

    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {

    }
}
