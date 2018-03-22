package de.espend.idea.php.annotation.doctrine.reference;

import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider;
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter;
import de.espend.idea.php.annotation.extension.parameter.PhpAnnotationReferenceProviderParameter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrineAnnotationFieldTypeProvider implements PhpAnnotationReferenceProvider {
    @Nullable
    @Override
    public PsiReference[] getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, PhpAnnotationReferenceProviderParameter referencesByElementParameter) {

        if(annotationPropertyParameter.getType() != AnnotationPropertyParameter.Type.PROPERTY_VALUE) {
            return null;
        }

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null || !propertyName.equals("type")) {
            return null;
        }

        String presentableFQN = annotationPropertyParameter.getPhpClass().getPresentableFQN();
        if(!presentableFQN.startsWith("\\")) {
            presentableFQN = "\\" + presentableFQN;
        }

        if(!presentableFQN.equals("\\Doctrine\\ORM\\Mapping\\Column")) {
            return null;
        }

        return new PsiReference[] {
            new DoctrineColumnTypeReference((StringLiteralExpression) annotationPropertyParameter.getElement())
        };
    }

    private static class DoctrineColumnTypeReference extends PsiPolyVariantReferenceBase<PsiElement> {

        @NotNull
        private final StringLiteralExpression psiElement;

        public DoctrineColumnTypeReference(@NotNull StringLiteralExpression psiElement) {
            super(psiElement);
            this.psiElement = psiElement;
        }

        @NotNull
        @Override
        public ResolveResult[] multiResolve(boolean b) {

            String contents = psiElement.getContents();
            if(StringUtils.isBlank(contents)) {
                return new ResolveResult[0];
            }

            return PsiElementResolveResult.createResults(
                DoctrineUtil.getColumnTypesTargets(psiElement.getProject(), contents)
            );
        }

        @NotNull
        @Override
        public Object[] getVariants() {
            return new Object[0];
        }
    }

}
