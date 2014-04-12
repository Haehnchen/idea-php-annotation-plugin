package de.espend.idea.php.annotation.extension;

import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.PhpAnnotationExtension;
import de.espend.idea.php.annotation.completion.parameter.CompletionParameter;
import de.espend.idea.php.annotation.reference.parameter.ReferencesByElementParameter;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


public class AnnotationClassProvider implements PhpAnnotationExtension {

    @Nullable
    @Override
    public Collection<String> getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter) {
        return null;
    }

    @Nullable
    @Override
    public Collection<PsiReference> getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null || !propertyName.equals("targetEntity")) {
            return null;
        }

        return new ArrayList<PsiReference>(Arrays.asList(
            new PhpClassServiceReference((StringLiteralExpression) annotationPropertyParameter.getElement()))
        );
    }

    public class PhpClassServiceReference extends PsiPolyVariantReferenceBase<PsiElement> {

        final private String content;

        public PhpClassServiceReference(StringLiteralExpression psiElement) {
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

}
