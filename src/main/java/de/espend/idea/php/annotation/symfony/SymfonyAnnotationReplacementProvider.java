package de.espend.idea.php.annotation.symfony;

import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.extension.PhpAnnotationDeprecatedReplacement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SymfonyAnnotationReplacementProvider implements PhpAnnotationDeprecatedReplacement {
    @NotNull
    @Override
    public Collection<PhpClass> findReplacementsFor(@NotNull PhpClass deprecatedClass) {

        if (deprecatedClass.getFQN().equals("\\Sensio\\Bundle\\FrameworkExtraBundle\\Configuration\\Route")) {
            return PhpIndex
                    .getInstance(deprecatedClass.getProject())
                    .getClassesByFQN("\\Symfony\\Component\\Routing\\Annotation\\Route");
        }

        return ContainerUtil.emptyList();
    }
}
