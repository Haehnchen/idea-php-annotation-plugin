package de.espend.idea.php.annotation.extension;

import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.annotation.symfony.quickFix.ReplaceRouteAnnotationQuickFix;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PhpAnnotationDeprecatedReplacement {
    @NotNull
    Collection<ReplaceRouteAnnotationQuickFix> findQuickFixesFor(@NotNull PhpDocTag phpDocTag, @NotNull PhpClass deprecatedClass);
}
