package de.espend.idea.php.annotation.navigation;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.extension.PhpAnnotationDocTagGotoHandler;
import de.espend.idea.php.annotation.extension.parameter.AnnotationDocTagGotoHandlerParameter;
import de.espend.idea.php.annotation.pattern.AnnotationPattern;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import de.espend.idea.php.annotation.util.PhpElementsUtil;
import de.espend.idea.php.annotation.util.PluginUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationGoToDeclarationHandler implements GotoDeclarationHandler {

    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {

        if(!PluginUtil.isEnabled(psiElement)) {
            return null;
        }

        // @Test(<foo>=)
        List<PsiElement> psiElements = new ArrayList<PsiElement>();
        if(AnnotationPattern.getDocAttribute().accepts(psiElement)) {
            this.addPropertyGoto(psiElement, psiElements);
        }

        // <@Test>
        // <@Test\Test>
        if (PlatformPatterns.psiElement(PhpDocElementTypes.DOC_TAG_NAME).withText(StandardPatterns.string().startsWith("@")).withLanguage(PhpLanguage.INSTANCE).accepts(psiElement)) {
            this.addDocTagNameGoto(psiElement, psiElements);
        }

        // @Route(name=<ClassName>::FOO)
        if (PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).beforeLeaf(PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STATIC)).withLanguage(PhpLanguage.INSTANCE).accepts(psiElement)) {
            this.addStaticClassTargets(psiElement, psiElements);
        }

        // @Route(name=ClassName::<FOO>)
        if (PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_IDENTIFIER).afterLeaf(PlatformPatterns.psiElement(PhpDocTokenTypes.DOC_STATIC)).withLanguage(PhpLanguage.INSTANCE).accepts(psiElement)) {
            this.addStaticClassConstTargets(psiElement, psiElements);
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    /**
     * Add goto for DocTag itself which should be the PhpClass and provide Extension
     *
     * @param psiElement origin DOC_TAG_NAME psi element
     * @param targets Goto targets
     */
    private void addDocTagNameGoto(PsiElement psiElement, List<PsiElement> targets) {

        PsiElement phpDocTagValue = psiElement.getContext();
        if(!(phpDocTagValue instanceof PhpDocTag)) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference((PhpDocTag) phpDocTagValue);
        if(phpClass == null) {
            return;
        }

        targets.add(phpClass);

        AnnotationDocTagGotoHandlerParameter parameter = new AnnotationDocTagGotoHandlerParameter((PhpDocTag) phpDocTagValue, phpClass, targets);
        for(PhpAnnotationDocTagGotoHandler phpAnnotationExtension : AnnotationUtil.EP_DOC_TAG_GOTO.getExtensions()) {
            phpAnnotationExtension.getGotoDeclarationTargets(parameter);
        }

    }

    /**
     * Add goto for property value which are Fields inside PhpClass
     *
     * @param psiElement origin DOC_IDENTIFIER psi element
     * @param targets Goto targets
     */
    private void addPropertyGoto(PsiElement psiElement, List<PsiElement> targets) {

        PhpDocTag phpDocTag = PsiTreeUtil.getParentOfType(psiElement, PhpDocTag.class);
        if(phpDocTag == null) {
            return;
        }

        PhpClass phpClass = AnnotationUtil.getAnnotationReference(phpDocTag);
        if(phpClass == null) {
            return;
        }

        for(Field field: phpClass.getFields()) {
            if(field.getName().equals(psiElement.getText())) {
                targets.add(field);
                return;
            }
        }
    }

    /**
     * Add class targets @Route(name=<ClassName>::FOO)
     *
     * @param psiElement DOC_IDENTIFIER
     * @param targets Goto targets
     */
    private void addStaticClassTargets(PsiElement psiElement, List<PsiElement> targets) {

        String text = psiElement.getText();
        PhpClass phpClass = PhpElementsUtil.getClassByContext(psiElement, text);
        if(phpClass != null) {
            targets.add(phpClass);
        }

    }

    /**
     * Add static field targets for @Route(name=ClassName::<FOO>)
     *
     * @param psiElement DOC_IDENTIFIER after DOC_STATIC
     * @param targets Goto targets
     */
    private void addStaticClassConstTargets(PsiElement psiElement, List<PsiElement> targets) {

        String constName = psiElement.getText();

        PsiElement docStatic = psiElement.getPrevSibling();
        if(docStatic != null && docStatic.getNode().getElementType() == PhpDocTokenTypes.DOC_STATIC) {
            PsiElement docIdentifier = docStatic.getPrevSibling();
            if(docIdentifier != null && docIdentifier.getNode().getElementType() == PhpDocTokenTypes.DOC_IDENTIFIER) {
                String className = docIdentifier.getText();
                PhpClass phpClass = PhpElementsUtil.getClassByContext(psiElement, className);
                if(phpClass != null) {
                    Field field = phpClass.findFieldByName(constName, true);
                    if(field != null) {
                        targets.add(field);
                    }
                }
            }
        }

    }

    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }

}
