package de.espend.idea.php.annotation.util;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        String defaultType = DoctrineUtil.guessFieldType(forElement);
        boolean isNullable = forElement.getType().isNullable();

        if (AnnotationUtil.useAttributeForGenerateDoctrineMetadata(file))  {
            String arguments = "type: '" + defaultType + "'";

            if (isNullable) {
                arguments += ", nullable: true";
            }

            addAttribute(document, forElement, beforeElement, "\\Doctrine\\ORM\\Mapping\\Column", arguments);

            PsiElement parent = forElement.getParent();

            Set<String> currentAttributes = PsiTreeUtil.findChildrenOfType(parent, PhpAttribute.class)
                .stream()
                .map(PhpAttribute::getFQN)
                .collect(Collectors.toSet());

            if (fieldName.equals("id")) {
                if (!currentAttributes.contains("\\Doctrine\\ORM\\Mapping\\GeneratedValue")) {
                    addAttribute(document, forElement, beforeElement, "\\Doctrine\\ORM\\Mapping\\GeneratedValue", "strategy: 'AUTO'");
                }

                if (!currentAttributes.contains("\\Doctrine\\ORM\\Mapping\\Id")) {
                    addAttribute(document, forElement, beforeElement, "\\Doctrine\\ORM\\Mapping\\Id", null);
                }
            }
        } else {
            if (fieldName.equals("id")) {
                PhpDocCommentAnnotation container = AnnotationUtil.getPhpDocCommentAnnotationContainer(forElement.getDocComment());
                if(container == null || container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\Id") == null) {
                    addPhpDocTag(forElement, document, file, beforeElement, "\\Doctrine\\ORM\\Mapping\\Id", null);
                }

                if(container == null || container.getPhpDocBlock("Doctrine\\ORM\\Mapping\\GeneratedValue") == null) {
                    addPhpDocTag(forElement, document, file, beforeElement, "\\Doctrine\\ORM\\Mapping\\GeneratedValue", "strategy=\"AUTO\"");
                }
            }

            String arguments = "type=\"" + defaultType + "\"";

            if (isNullable) {
                arguments += ", nullable=true";
            }

            addPhpDocTag(forElement, document, file, beforeElement, "\\Doctrine\\ORM\\Mapping\\Column", arguments);
        }
    }

    public static void addClassOrmDocs(@NotNull PhpClass forElement, @NotNull Document document, @NotNull PsiFile file)
    {
        String entityName = forElement.getPresentableFQN() + "Repository";
        PhpClass phpClass = PhpElementsUtil.getClass(forElement.getProject(), entityName);

        if (AnnotationUtil.useAttributeForGenerateDoctrineMetadata(file)) {
            String repositoryClass = null;

            if (phpClass != null) {
                repositoryClass = "repositoryClass: " + phpClass.getFQN() + "::class";

                // import not working?
                PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(forElement);
                if (scopeForUseOperator != null) {
                    PhpElementsUtil.insertUseIfNecessary(scopeForUseOperator, phpClass.getFQN(), null);
                    PsiDocumentManager.getInstance(forElement.getProject()).doPostponedOperationsAndUnblockDocument(document);

                    String phpDocTagName = getQualifiedName(forElement, phpClass.getFQN());
                    if (phpDocTagName != null) {
                        repositoryClass = "repositoryClass: " + phpDocTagName + "::class";
                    }
                }
            }

            addAttribute(document, forElement, forElement, "\\Doctrine\\ORM\\Mapping\\Table", "name: '" + DoctrineUtil.underscore(forElement.getName()) + "'");
            addAttribute(document, forElement, forElement, "\\Doctrine\\ORM\\Mapping\\Entity", repositoryClass);
        } else {
            String repositoryClass = null;

            if(phpClass != null) {
                repositoryClass = "repositoryClass=\"" + phpClass.getName() + "\"";
            }

            addPhpDocTag(forElement, document, file, forElement, "\\Doctrine\\ORM\\Mapping\\Entity", repositoryClass);
            addPhpDocTag(forElement, document, file, forElement, "\\Doctrine\\ORM\\Mapping\\Table", "name=\"" + DoctrineUtil.underscore(forElement.getName()) + "\"");
        }
     }

    public static void addClassEmbeddedDocs(@NotNull PhpClass forElement, @NotNull Document document, @NotNull PsiFile file)
    {
        if (AnnotationUtil.useAttributeForGenerateDoctrineMetadata(file)) {
            addAttribute(document, forElement, forElement, "\\Doctrine\\ORM\\Mapping\\Embeddable", null);
        } else {
            addPhpDocTag(forElement, document, file, forElement, "\\Doctrine\\ORM\\Mapping\\Embeddable", null);
        }
    }

    private static void addAttribute(@NotNull Document document, @NotNull PhpNamedElement forElement, @NotNull PsiElement beforeElement, @NotNull String annotationClass, @Nullable String p)
    {
        String phpDocTagName = getQualifiedName(forElement, annotationClass);
        if(phpDocTagName == null) {
            return;
        }

        PhpAttributesList phpPsiFromText = PhpPsiElementFactory.createAttributesList(forElement.getProject(), phpDocTagName + (p != null ? "(" + p + ")" : ""));
        PsiElement parent = beforeElement.getParent();
        parent.addBefore(phpPsiFromText, beforeElement);

        PsiDocumentManager.getInstance(forElement.getProject()).doPostponedOperationsAndUnblockDocument(document);
        PsiDocumentManager.getInstance(forElement.getProject()).commitDocument(document);

        // attributes are not wrapped into a parent node on class context
        if (forElement instanceof PhpClass) {
            // on class scope: class Foobar {}
            List<PhpAttributesList> childrenOfTypeAsList = PsiTreeUtil.getChildrenOfTypeAsList(forElement, PhpAttributesList.class);
            CodeStyleManager.getInstance(forElement.getProject()).reformatRange(forElement, childrenOfTypeAsList.get(0).getTextRange().getStartOffset(), childrenOfTypeAsList.get(childrenOfTypeAsList.size() - 1).getNextPsiSibling().getTextRange().getEndOffset());
        } else {
            // on attribute scope: private $foo;
            PhpClassFieldsList f = PsiTreeUtil.getParentOfType(forElement, PhpClassFieldsList.class);
            PhpAttributesList childOfType = PsiTreeUtil.findChildOfType(f, PhpAttributesList.class);
            CodeStyleManager.getInstance(forElement.getProject()).reformatNewlyAddedElement(f.getNode(), childOfType.getNode());
        }
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
            PsiElement docParent = PsiTreeUtil.findFirstParent(atElement, true, element -> ((element instanceof PhpDocComment)) || ((element instanceof PhpFile)));
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

        PhpReference reference = PhpPsiUtil.getParentByCondition(psiElement, false, PhpReference.INSTANCEOF, null);
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

    /**
     * Workaround for STATIC node element: @Foo(foo::bar) and @Foo(name={foo::bar});
     * see: WI-32801
     */
    public static boolean isDocStaticElement(@NotNull PsiElement psiElement) {
        return PhpPsiUtil.isOfType(psiElement, PhpDocTokenTypes.DOC_STATIC) ||
            (PhpPsiUtil.isOfType(psiElement, PhpDocTokenTypes.DOC_TEXT) && "::".equals(psiElement.getText()));
    }

    /**
     * Extract namespace+class @DateTime(Foo\Ker<caret>nel::VERSION)
     */
    @Nullable
    public static String getNamespaceForDocIdentifier(@NotNull PsiElement psiElement) {
        if(psiElement.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER) {
            return null;
        }

        PsiElement child = psiElement.getPrevSibling();
        List<String> namespaces = new ArrayList<>(Collections.singletonList(psiElement.getText()));
        while (child != null) {
            if(!isValidClassText(child)) {
                Collections.reverse(namespaces);
                return StringUtils.join(namespaces, null);
            }

            namespaces.add(child.getText());
            child = child.getPrevSibling();
        }

        return StringUtils.join(namespaces, null);
    }

    /**
     * Extract namespace+class @DateTime(F<caret>oo\Kernel::VERSION) => "F<caret>oo\Kerne"
     */
    @Nullable
    public static String getNamespaceForDocIdentifierAtStart(@NotNull PsiElement psiElement) {
        if(psiElement.getNode().getElementType() != PhpDocTokenTypes.DOC_IDENTIFIER) {
            return null;
        }

        PsiElement prevSibling = psiElement.getPrevSibling();

        List<String> namespaces = new ArrayList<>();

        if (prevSibling != null && prevSibling.getNode().getElementType() == PhpDocTokenTypes.DOC_NAMESPACE) {
            namespaces.add("\\");
        }

        PsiElement child = psiElement.getNextSibling();
        namespaces.add(psiElement.getText());
        while (child != null) {
            // end at static
            if (child.getNode().getElementType() == PhpDocTokenTypes.DOC_STATIC) {
                return StringUtils.join(namespaces, null);
            }

            if(!isValidClassTextReverse(child)) {
                return null;
            }

            namespaces.add(child.getText());
            child = child.getNextSibling();
        }

        return null;
    }

    /**
     * Starting right and search left until we find a stopping char "Foo\Ke<caret>rnel::VERSION" => "Foo\Kernel"
     */
    private static boolean isValidClassText(@NotNull PsiElement psiElement) {
        IElementType elementType = psiElement.getNode().getElementType();
        if(elementType == PhpDocTokenTypes.DOC_NAMESPACE) {
            return true;
        }

        // non textual chars
        if(!(elementType == PhpDocTokenTypes.DOC_TEXT || elementType == PhpDocTokenTypes.DOC_IDENTIFIER)){
            return false;
        }

        // NAMESPACE fix inside array: @FOO(a={Foo\Foo})
        String text = psiElement.getText();
        if(elementType == PhpDocTokenTypes.DOC_TEXT && "\\".equals(text)) {
            return true;
        }

        return text.matches("^\\w+$");
    }

    /**
     * Starting left and search right until we find a stopping on the left like "static" element "F<caret>oo\Kernel::VERSION" => "Foo\Kernel"
     */
    private static boolean isValidClassTextReverse(@NotNull PsiElement psiElement) {
        return psiElement.getNode().getElementType() != PhpDocTokenTypes.DOC_STATIC && isValidClassText(psiElement);
    }

    /**
     * Extract namespace+class @DateTime(F<caret>oo\Kernel::VERSION) => "F<caret>oo\Kernel"
     */
    public static boolean isFirstIdentifierInNamespace(@NotNull PsiElement psiElement) {
        PsiElement prevSibling = psiElement.getPrevSibling();
        if (prevSibling == null) {
            return true;
        }

        if (prevSibling.getNode().getElementType() == PhpDocTokenTypes.DOC_NAMESPACE) {
            prevSibling = prevSibling.getPrevSibling();
        }

        return prevSibling == null || !isValidClassText(prevSibling);
    }
}
