package de.espend.idea.php.annotation.extension;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpNamedElementImpl;
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
import java.util.List;


public class DoctrineAnnotationTypeProvider implements PhpAnnotationExtension {

    @Nullable
    @Override
    public Collection<String> getPropertyValueCompletions(AnnotationPropertyParameter annotationPropertyParameter, CompletionParameter completionParameter) {
        return null;
    }

    @Nullable
    @Override
    public Collection<PsiReference> getPropertyReferences(AnnotationPropertyParameter annotationPropertyParameter, ReferencesByElementParameter referencesByElementParameter) {

        String propertyName = annotationPropertyParameter.getPropertyName();
        if(propertyName == null || !propertyName.equals("repositoryClass")) {
            return null;
        }

        String presentableFQN = annotationPropertyParameter.getPhpClass().getPresentableFQN();
        if(!PhpLangUtil.equalsClassNames("Doctrine\\ORM\\Mapping\\Entity", presentableFQN)) {
            return null;
        }

        List<PsiReference> psiReferences = new ArrayList<PsiReference>();
        psiReferences.add(new PhpClassServiceReference((StringLiteralExpression) annotationPropertyParameter.getElement()));

        return psiReferences;
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

            PhpClass phpClass = getAnnotationRepositoryClass((StringLiteralExpression) getElement(), content);
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

            List<LookupElement> lookupElements = new ArrayList<LookupElement>();

            for(PhpClass phpClass: PhpIndex.getInstance(getElement().getProject()).getAllSubclasses("\\Doctrine\\Common\\Persistence\\ObjectRepository")) {
                String presentableFQN = phpClass.getPresentableFQN();
                if(presentableFQN != null) {
                    lookupElements.add(LookupElementBuilder.create(presentableFQN).withIcon(PhpIcons.CLASS_ICON));
                }
            }

            return lookupElements.toArray();
        }
    }

    public static PhpClass getAnnotationRepositoryClass(StringLiteralExpression phpDocString, String modelName) {

        // \ns\Class fine we dont need to resolve classname we are in global context
        if(modelName.startsWith("\\")) {
            return PhpElementsUtil.getClassInterface(phpDocString.getProject(), modelName);
        }

        // try class shortcut: ns\Class
        PhpClass phpClass = PhpElementsUtil.getClassInterface(phpDocString.getProject(), modelName);
        if(phpClass != null) {
            return phpClass;
        }

        PhpDocComment inClass = PsiTreeUtil.getParentOfType(phpDocString, PhpDocComment.class);
        if(inClass == null) {
            return null;
        }

        // doc before class
        PhpPsiElement phpClassElement = inClass.getNextPsiSibling();
        if(phpClassElement instanceof PhpClass) {
            String className = ((PhpClass) phpClassElement).getNamespaceName() + modelName;
            return PhpElementsUtil.getClassInterface(phpDocString.getProject(), className);
        }

        // eg property, method
        PhpClass insidePhpClass = PsiTreeUtil.getParentOfType(phpClassElement, PhpClass.class);
        if(insidePhpClass != null) {
            String className = insidePhpClass.getNamespaceName() + modelName;
            return PhpElementsUtil.getClassInterface(phpDocString.getProject(), className);
        }

        return null;

    }
}
