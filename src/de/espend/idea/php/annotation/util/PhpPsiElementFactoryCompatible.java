package de.espend.idea.php.annotation.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.util.PhpContractUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpPsiElementFactoryCompatible {

    @NotNull
    public static PhpUseList createUseStatementWithKeyword(@NotNull Project project, @Nullable String keyword, @NotNull String fqn, @Nullable String alias) {

        PhpContractUtil.assertFqn(fqn);
        PhpContractUtil.assertNoQualifier(alias);

        StringBuilder builder = new StringBuilder("<?php\nuse ");
        if (StringUtil.isNotEmpty(keyword)) {
            builder.append(keyword).append(" ");
        }
        builder.append(StringUtil.trimStart(fqn, "\\"));
        if (StringUtil.isNotEmpty(alias)) {
            builder.append(" as ").append(alias);
        }
        builder.append(";\n");

        return PhpPsiElementFactory.createFromText(project, PhpUseList.class, builder.toString());

    }

}
