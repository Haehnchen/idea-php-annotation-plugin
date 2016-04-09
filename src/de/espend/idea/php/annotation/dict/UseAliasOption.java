package de.espend.idea.php.annotation.dict;

import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class UseAliasOption {

    @Nullable
    private String className;

    @Nullable
    private String alias;

    public UseAliasOption() {
    }

    public UseAliasOption(@Nullable String className, @Nullable String alias) {
        this.className = className;
        this.alias = alias;
    }

    @Nullable
    public String getClassName() {
        return className;
    }

    @Nullable
    public String getAlias() {
        return alias;
    }

    public void setClassName(@Nullable String className) {
        this.className = className;
    }

    public void setAlias(@Nullable String alias) {
        this.alias = alias;
    }
}
