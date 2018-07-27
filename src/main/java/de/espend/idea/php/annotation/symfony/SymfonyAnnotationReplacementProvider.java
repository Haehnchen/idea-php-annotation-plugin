package de.espend.idea.php.annotation.symfony;

import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.extension.PhpAnnotationDeprecatedReplacement;
import de.espend.idea.php.annotation.symfony.quickFix.ReplaceRouteAnnotationQuickFix;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class SymfonyAnnotationReplacementProvider implements PhpAnnotationDeprecatedReplacement {
    @NotNull
    @Override
    public Collection<ReplaceRouteAnnotationQuickFix> findQuickFixesFor(@NotNull PhpDocTag phpDocTag, @NotNull PhpClass deprecatedClass) {

        if (!deprecatedClass.getFQN().equals("\\Sensio\\Bundle\\FrameworkExtraBundle\\Configuration\\Route")) {
            return ContainerUtil.emptyList();
        }

        Collection<PhpClass> classesByFQN = PhpIndex
                .getInstance(deprecatedClass.getProject())
                .getClassesByFQN("\\Symfony\\Component\\Routing\\Annotation\\Route");

        return classesByFQN
                .stream()
                .map(replacementClass -> new ReplaceRouteAnnotationQuickFix(phpDocTag, deprecatedClass, replacementClass))
                .collect(Collectors.toList());
    }
}
