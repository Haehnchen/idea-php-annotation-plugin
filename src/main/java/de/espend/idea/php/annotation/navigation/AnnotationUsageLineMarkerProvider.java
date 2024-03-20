package de.espend.idea.php.annotation.navigation;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.AnnotationUsageIndex;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationUsageLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> psiElements, @NotNull Collection<? super LineMarkerInfo<?>> results) {
        for(PsiElement psiElement: psiElements) {
            if(!getClassNamePattern().accepts(psiElement)) {
                continue;
            }

            if (!(psiElement.getContext() instanceof PhpClass phpClass)) {
                continue;
            }

            boolean isAnnotationOrAttribute = AnnotationUtil.isAnnotationClass(phpClass)
                || !phpClass.getAttributes("\\Attribute").isEmpty();

            if (!isAnnotationOrAttribute) {
                return;
            }

            String fqn = StringUtils.stripStart(phpClass.getFQN(), "\\");

            // find one index annotation class and stop processing on first match
            final boolean[] processed = {false};
            FileBasedIndex.getInstance().getFilesWithKey(AnnotationUsageIndex.KEY, new HashSet<>(Collections.singletonList(fqn)), virtualFile -> {
                processed[0] = true;

                // stop on first match
                return false;
            }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(psiElement.getProject()), PhpFileType.INSTANCE));

            // we found at least one target to provide lazy target linemarker
            if(processed[0]) {
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(PhpIcons.IMPLEMENTS)
                    .setTargets(NotNullLazyValue.lazy(() -> AnnotationUtil.getImplementationsForAnnotation(psiElement.getProject(), fqn)))
                    .setTooltipText("Navigate to implementations");

                results.add(builder.createLineMarkerInfo(psiElement));
            }
        }
    }

    /**
     * class "Foo" extends
     */
    private static PsiElementPattern.Capture<PsiElement> getClassNamePattern() {
        return PlatformPatterns
            .psiElement(PhpTokenTypes.IDENTIFIER)
            .afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class),
                PlatformPatterns.psiElement(PhpTokenTypes.kwCLASS)
            )
            .withParent(PhpClass.class)
            .withLanguage(PhpLanguage.INSTANCE);
    }
}