package de.espend.idea.php.annotation.extension.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.resolve.PhpResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                LookupElementBuilder lookupElement = LookupElementBuilder.createWithIcon(field);
                lookupElement = attachLookupInformation(field, lookupElement);
                lookupElements.add(lookupElement);
            }
        }

        return lookupElements.toArray();
    }

    private LookupElementBuilder attachLookupInformation(Field field, LookupElementBuilder lookupElement) {

        // get some more presentable completion information
        // dont resolve docblocks; just extract them from doc comment
        PhpDocComment docBlock = field.getDocComment();

        if(docBlock == null) {
            return lookupElement;
        }

        String text = docBlock.getText();

        // column type
        Matcher matcher = Pattern.compile("type=[\"|']([\\w_\\\\]+)[\"|']").matcher(text);
        if (matcher.find()) {
            lookupElement = lookupElement.withTypeText(matcher.group(1), true);
        }

        // targetEntity name
        matcher = Pattern.compile("targetEntity=[\"|']([\\w_\\\\]+)[\"|']").matcher(text);
        if (matcher.find()) {
            lookupElement = lookupElement.withTypeText(matcher.group(1), true);
            lookupElement = lookupElement.withBoldness(true);
        }

        // relation type
        matcher = Pattern.compile("((Many|One)To(Many|One))\\(").matcher(text);
        if (matcher.find()) {
            lookupElement = lookupElement.withTailText(matcher.group(1), true);
        }

        return lookupElement;
    }

}
