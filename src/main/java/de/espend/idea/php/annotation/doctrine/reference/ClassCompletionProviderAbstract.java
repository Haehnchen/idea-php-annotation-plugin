package de.espend.idea.php.annotation.doctrine.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpCompletionUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import de.espend.idea.php.annotation.reference.references.PhpClassReference;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public abstract class ClassCompletionProviderAbstract implements PhpAnnotationReferenceProvider, PhpAnnotationCompletionProvider {
    @Nullable
    @Override
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter parameter, PhpAnnotationReferenceProviderParameter referencesByElementParameter) {
        if(!supports(parameter)) {
            return new PsiReference[0];
        }

        PsiElement element = parameter.getElement();
        if(!(element instanceof StringLiteralExpression) || StringUtils.isBlank(((StringLiteralExpression) element).getContents())) {
            return new PsiReference[0];
        }

        return new PsiReference[] {
            new PhpClassReference((StringLiteralExpression) element)
        };
    }

    @Override
    public void getPropertyValueCompletions(AnnotationPropertyParameter parameter, AnnotationCompletionProviderParameter completionParameter) {
        if(!supports(parameter)) {
            return;
        }

        String className = "";
        PsiElement element = parameter.getElement();
        if(element instanceof StringLiteralExpression) {
            className = ((StringLiteralExpression) element).getContents();
        }

        PhpIndex phpIndex = PhpIndex.getInstance(parameter.getProject());
        PhpCompletionUtil.addClasses(className, completionParameter.getResult(), phpIndex, null);
    }

    abstract boolean supports(AnnotationPropertyParameter parameter);
}
