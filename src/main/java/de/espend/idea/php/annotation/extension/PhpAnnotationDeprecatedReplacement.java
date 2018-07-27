package de.espend.idea.php.annotation.extension;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PhpAnnotationDeprecatedReplacement {
    @NotNull
    Collection<PhpClass> findReplacementsFor(@NotNull PhpClass deprecatedClass);
}
