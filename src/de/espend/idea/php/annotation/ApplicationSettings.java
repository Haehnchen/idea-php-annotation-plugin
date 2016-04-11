package de.espend.idea.php.annotation;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@State(name = "PhpAnnotationsPlugin", storages = @Storage(id = "php-annotations-plugin", file = "$APP_CONFIG$/php-annotations.xml"))
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings> {

    public boolean appendRoundBracket = true;

    public List<UseAliasOption> useAliasOptions = new ArrayList<UseAliasOption>();

    /**
     * First user change, so that can provide defaults
     */
    public boolean provideDefaults = true;

    @Nullable
    @Override
    public ApplicationSettings getState() {
        return this;
    }

    @Override
    public void loadState(ApplicationSettings insightApplicationSettings) {
        XmlSerializerUtil.copyBean(insightApplicationSettings, this);
    }

    public static ApplicationSettings getInstance() {
        return ServiceManager.getService(ApplicationSettings.class);
    }

    public static Collection<UseAliasOption> getDefaultUseAliasOption() {
        Collection<UseAliasOption> options = new ArrayList<UseAliasOption>();

        options.add(new UseAliasOption("Symfony\\Component\\Validator\\Constraints", "Assert", true));
        options.add(new UseAliasOption("Doctrine\\ORM\\Mapping", "ORM", true));
        options.add(new UseAliasOption("JMS\\DiExtraBundle\\Annotation", "DI", true));
        options.add(new UseAliasOption("JMS\\Serializer\\Annotation", "Serializer", true));
        options.add(new UseAliasOption("Gedmo\\Mapping\\Annotation", "Gedmo", true));

        return options;
    }

    @NotNull
    public static Collection<UseAliasOption> getUseAliasOptionsWithDefaultFallback() {
        if(getInstance().provideDefaults && getInstance().useAliasOptions.size() == 0) {
            return getDefaultUseAliasOption();
        }

        return getInstance().useAliasOptions;
    }
}
