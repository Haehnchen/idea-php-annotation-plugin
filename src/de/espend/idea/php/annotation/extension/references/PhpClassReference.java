package de.espend.idea.php.annotation.extension.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

public class PhpClassReference extends PsiPolyVariantReferenceBase<PsiElement> {

    final private String content;

    public PhpClassReference(StringLiteralExpression psiElement) {
        super(psiElement);
        this.content = psiElement.getContents();
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {

        PhpClass phpClass = PhpElementsUtil.getClassInsideAnnotation((StringLiteralExpression) getElement(), content);
        if(phpClass == null) {
            return new ResolveResult[0];
        }

        return new ResolveResult[] {
            new PsiElementResolveResult(phpClass)
        };
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}