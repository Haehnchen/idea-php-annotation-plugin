package de.espend.idea.php.annotation.doctrine.reference.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpPromotedFieldParameterImpl;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.dict.PhpDocTagAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DoctrinePhpClassFieldReference extends PsiPolyVariantReferenceBase<PsiElement> {

    final PhpClass phpClass;
    final String content;

    public DoctrinePhpClassFieldReference(StringLiteralExpression psiElement, PhpClass phpClass) {
        super(psiElement);
        this.phpClass = phpClass;
        this.content = psiElement.getContents();
    }

    @NotNull
    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        List<PsiElement> psiElementList = this.phpClass.getFields().stream()
            .filter(field -> !field.isConstant() && content.equals(field.getName()))
            .collect(Collectors.toList());

        return PsiElementResolveResult.createResults(psiElementList);
    }

    @NotNull
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElement> lookupElements = new ArrayList<>();

        PhpClass entity = PsiTreeUtil.getParentOfType(getElement(), PhpClass.class);
        for (Field field: this.phpClass.getFields().stream().filter(field -> !field.isConstant()).toList()) {
            LookupElementBuilder lookupElement = LookupElementBuilder.createWithIcon(field);
            lookupElement = attachLookupInformation(field, lookupElement, entity);
            lookupElements.add(lookupElement);
        }

        return lookupElements.toArray();
    }

    private LookupElementBuilder attachLookupInformation(@NotNull Field field, @NotNull LookupElementBuilder lookupElement, @Nullable PhpClass entity) {
        boolean matchForeignType = false;

        if (entity != null) {
            Project project = field.getProject();
            String fqn = entity.getFQN();

            for (String type : PhpIndex.getInstance(project).completeType(project, field.getType(), new HashSet<>()).getTypes()) {
                if (type.equals(fqn) || type.equals(fqn + "[]")) {
                    matchForeignType = true;
                    break;
                }
            }
        }

        // get some more presentable completion information
        PhpDocComment docBlock = field.getDocComment();
        if (docBlock != null) {
            PhpDocCommentAnnotation phpDocCommentAnnotation = AnnotationUtil.getPhpDocCommentAnnotationContainer(docBlock);
            if (phpDocCommentAnnotation == null) {
                return lookupElement;
            }

            // search column type
            PhpDocTagAnnotation phpDocTagAnnotation = phpDocCommentAnnotation.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Column");
            if (phpDocTagAnnotation != null) {
                String value = phpDocTagAnnotation.getPropertyValue("type");
                if (value != null) {
                    lookupElement = lookupElement.withTypeText(value, true);
                }
            }

            // search for relations
            PhpDocTagAnnotation relation = phpDocCommentAnnotation.getFirstPhpDocBlock(DoctrineUtil.DOCTRINE_RELATION_FIELDS);
            if (relation != null) {
                String value = relation.getPropertyValue("targetEntity");
                if (value != null) {
                    lookupElement = lookupElement.withTypeText(StringUtils.stripStart(value, "\\"), true);
                    lookupElement = lookupElement.withBoldness(matchForeignType);
                }

                lookupElement = lookupElement.withTailText(String.format("(%s)", relation.getPhpClass().getName()), true);
            }
        } else  {
            List<PhpAttributesList> childrenOfTypeAsList = null;

            if (field instanceof PhpPromotedFieldParameterImpl) {
                childrenOfTypeAsList = PsiTreeUtil.getChildrenOfTypeAsList(field, PhpAttributesList.class);
            } else if (field.getParent() instanceof PhpPsiElement phpPsiElement) {
                childrenOfTypeAsList = PsiTreeUtil.getChildrenOfTypeAsList(phpPsiElement, PhpAttributesList.class);
            }

            if (childrenOfTypeAsList !=  null) {
                for (PhpAttributesList phpAttributesList : childrenOfTypeAsList) {
                    for (PhpAttribute attribute : phpAttributesList.getAttributes("\\Doctrine\\ORM\\Mapping\\Column")) {
                        String value = PhpElementsUtil.getAttributeArgumentStringByName(attribute, "type");
                        if (value != null) {
                            lookupElement = lookupElement.withTypeText(value, true);
                        }
                    }

                    for (String doctrineRelationField : DoctrineUtil.DOCTRINE_RELATION_FIELDS) {
                        for (PhpAttribute attribute : phpAttributesList.getAttributes(doctrineRelationField)) {
                            String value = PhpElementsUtil.getAttributeArgumentStringByName(attribute, "targetEntity");
                            if (value != null) {
                                lookupElement = lookupElement.withTypeText(StringUtils.stripStart(value, "\\"), true);
                                lookupElement = lookupElement.withBoldness(matchForeignType);
                            }
                        }
                    }
                }
            }
        }


        return lookupElement;
    }

}
