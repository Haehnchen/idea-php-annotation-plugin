package de.espend.idea.php.annotation.doctrine.reference.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineRepositoryReference extends PsiPolyVariantReferenceBase<PsiElement> {

    final private String content;

    public DoctrineRepositoryReference(StringLiteralExpression psiElement) {
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

        List<LookupElement> lookupElements = PhpIndex.getInstance(getElement().getProject())
            .getAllSubclasses("\\Doctrine\\Common\\Persistence\\ObjectRepository").stream()
            .map(phpClass -> LookupElementBuilder.create(phpClass.getPresentableFQN()).withIcon(PhpIcons.CLASS_ICON))
            .collect(Collectors.toList());

        return lookupElements.toArray();
    }
}
