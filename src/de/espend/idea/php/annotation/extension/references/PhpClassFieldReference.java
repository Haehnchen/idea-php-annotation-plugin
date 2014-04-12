package de.espend.idea.php.annotation.extension.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.resolve.PhpResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PhpClassFieldReference extends PsiPolyVariantReferenceBase<PsiElement> {

    final PhpClass phpClass;
    final String content;

    public PhpClassFieldReference(StringLiteralExpression psiElement, PhpClass phpClass) {
        super(psiElement);
        this.phpClass = phpClass;
        this.content = psiElement.getContents();
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {

        List<PsiElement> psiElementList = new ArrayList<PsiElement>();

        for(Field field: this.phpClass.getFields()) {
            if(!field.isConstant() && content.equals(field.getName())) {
                psiElementList.add(field);
            }
        }

        return PhpResolveResult.createResults(psiElementList);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> lookupElements = new ArrayList<LookupElement>();

        for(Field field: this.phpClass.getFields()) {
            if(!field.isConstant()) {
                lookupElements.add(LookupElementBuilder.createWithIcon(field));
            }
        }

        return lookupElements.toArray();
    }

}
