package de.espend.idea.php.annotation.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.parser.PhpStubElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.php.annotation.dict.PhpDocCommentAnnotation;
import de.espend.idea.php.annotation.doctrine.util.DoctrineUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpDocUtil {

    public static void addPropertyOrmDocs(@NotNull Field forElement, @NotNull Document document, @NotNull PsiFile file) {

        PsiElement beforeElement = getBeforeElement(forElement);
        if (beforeElement == null) {
            return;
        }

        String fieldName = forElement.getName();
        String defaultType = DoctrineUtil.guessFieldType(fieldName);

        if(fieldName.equals("id")) {

            PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(forElement.getDocComment());
            if(container == null || container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Id") == null) {
                addPhpDocTag(forElement, document, file, beforeElement, "\\Doctrine\\ORM\\Mapping\\Id", null);
            }

            if(container == null || container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\GeneratedValue") == null) {
                addPhpDocTag(forElement, document, file, beforeElement, "\\Doctrine\\ORM\\Mapping\\GeneratedValue", "strategy=\"AUTO\"");
            }

        }

        addPhpDocTag(forElement, document, file, beforeElement, "\\Doctrine\\ORM\\Mapping\\Column", "type=\"" + defaultType + "\"");

    }

    public static void addClassOrmDocs(@NotNull PhpClass forElement, @NotNull Document document, @NotNull PsiFile file)
    {

        String repositoryClass = null;
        String entityName = forElement.getPresentableFQN() + "Repository";
        PhpClass phpClass = PhpElementsUtil.getClass(forElement.getProject(), entityName);
        if(phpClass != null) {
            repositoryClass = "repositoryClass=\"" + phpClass.getName() + "\"";
        }

        addPhpDocTag(forElement, document, file, forElement, "\\Doctrine\\ORM\\Mapping\\Entity", repositoryClass);
        addPhpDocTag(forElement, document, file, forElement, "\\Doctrine\\ORM\\Mapping\\Table", "name=\"" + DoctrineUtil.underscore(forElement.getName()) + "\"");
    }

    private static void addPhpDocTag(@NotNull PhpNamedElement forElement, @NotNull Document document, @NotNull PsiFile file, @NotNull  PsiElement beforeElement, @NotNull String annotationClass, @Nullable String tagParameter) {

        String phpDocTagName = getQualifiedName(forElement, annotationClass);
        if(phpDocTagName == null) {
            return;
        }

        String tagString = "@" + phpDocTagName;
        if(tagParameter != null) {
            tagString += "(" + tagParameter + ")";
        }

        PhpDocComment docComment = forElement.getDocComment();

        if(docComment != null)  {

            PsiElement elementToInsert = PhpPsiElementFactory.createFromText(forElement.getProject(), PhpDocTag.class, "/** " + tagString + " */\\n");
            if(elementToInsert == null) {
                return;
            }

            PsiElement fromText = PhpPsiElementFactory.createFromText(forElement.getProject(), PhpDocTokenTypes.DOC_LEADING_ASTERISK, "/** \n * @var */");
            docComment.addBefore(fromText, docComment.getLastChild());
            docComment.addBefore(elementToInsert, docComment.getLastChild());

            PsiDocumentManager.getInstance(forElement.getProject()).doPostponedOperationsAndUnblockDocument(document);
            PsiDocumentManager.getInstance(forElement.getProject()).commitDocument(document);

            return;
        }

        // new PhpDoc see PhpDocCommentGenerator
        docComment = PhpPsiElementFactory.createFromText(forElement.getProject(), PhpDocComment.class, "/**\n " + tagString + " \n */");
        if(docComment == null) {
            return;
        }

        PsiElement parent = beforeElement.getParent();
        int atOffset = beforeElement.getTextRange().getStartOffset() + 1;
        parent.addBefore(docComment, beforeElement);
        PsiDocumentManager.getInstance(forElement.getProject()).doPostponedOperationsAndUnblockDocument(document);
        PsiElement atElement = file.findElementAt(atOffset);
        if (atElement != null)            {
            PsiElement docParent = PsiTreeUtil.findFirstParent(atElement, true, new Condition<PsiElement>() {
                public boolean value(PsiElement element) {
                    return ((element instanceof PhpDocComment)) || ((element instanceof PhpFile));
                }
            });
            if ((docParent instanceof PhpDocComment)) {
                CodeStyleManager.getInstance(forElement.getProject()).reformatNewlyAddedElement(docParent.getParent().getNode(), docParent.getNode());
            }
        }

        PsiDocumentManager.getInstance(forElement.getProject()).doPostponedOperationsAndUnblockDocument(document);
        PsiDocumentManager.getInstance(forElement.getProject()).commitDocument(document);

    }

    /**
     * Get class path on "use" path statement
     */
    @Nullable
    public static String getQualifiedName(@NotNull PsiElement psiElement, @NotNull String fqn) {

        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(psiElement);
        if (scopeForUseOperator == null) {
            return null;
        }

        PhpReference reference = PhpPsiUtil.getParentByCondition(psiElement, false, PhpReference.INSTANCEOF);
        String qualifiedName = PhpCodeInsightUtil.createQualifiedName(scopeForUseOperator, fqn, reference, false);
        if (!PhpLangUtil.isFqn(qualifiedName)) {
            return qualifiedName;
        }

        // @TODO: remove full fqn fallback
        if(qualifiedName.startsWith("\\")) {
            qualifiedName = qualifiedName.substring(1);
        }

        return qualifiedName;
    }

    @Nullable
    private static PsiElement getBeforeElement(@NotNull PsiElement element)
    {
        PsiElement parent = element.getParent();

        if (((element instanceof Field)) || (element.getNode().getElementType() == PhpStubElementTypes.DEFINE)) {
            return parent;
        }

        return null;
    }


}
