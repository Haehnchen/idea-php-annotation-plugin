package de.espend.idea.php.annotation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.extension.PhpAnnotationUseAlias;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@State(name = "PhpAnnotationsPlugin", storages = @Storage("PhpAnnotationsPlugin.xml"))
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings> {

    public boolean appendRoundBracket = true;

    public List<UseAliasOption> useAliasOptions = new ArrayList<>();

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
    public void loadState(@NotNull ApplicationSettings insightApplicationSettings) {
        XmlSerializerUtil.copyBean(insightApplicationSettings, this);
    }

    public static ApplicationSettings getInstance() {
        return ApplicationManager.getApplication().getService(ApplicationSettings.class);
    }

    public static Collection<UseAliasOption> getDefaultUseAliasOption() {
        Collection<UseAliasOption> options = new ArrayList<>();

        options.add(new UseAliasOption("Symfony\\Component\\Validator\\Constraints", "Assert", true));
        options.add(new UseAliasOption("Doctrine\\ORM\\Mapping", "ORM", true));
        options.add(new UseAliasOption("JMS\\DiExtraBundle\\Annotation", "DI", true));
        options.add(new UseAliasOption("JMS\\Serializer\\Annotation", "Serializer", true));
        options.add(new UseAliasOption("Gedmo\\Mapping\\Annotation", "Gedmo", true));
        options.add(new UseAliasOption("Vich\\UploaderBundle\\Mapping\\Annotation", "Vich", true));
        options.add(new UseAliasOption("FOS\\RestBundle\\Controller\\Annotations", "Rest", true));
        options.add(new UseAliasOption("Swagger\\Annotations", "SWG", true));
        options.add(new UseAliasOption("OpenApi\\Annotations", "OA", true));

        for (PhpAnnotationUseAlias extensions: AnnotationUtil.EP_USE_ALIASES.getExtensions()) {
            options.addAll(
                extensions.getAliases()
                .entrySet()
                .stream()
                .map(entry -> new UseAliasOption(entry.getValue(), entry.getKey(), true))
                .toList()
            );
        }

        return options;
    }

    @NotNull
    public static Collection<UseAliasOption> getUseAliasOptionsWithDefaultFallback() {
        if(getInstance().provideDefaults && getInstance().useAliasOptions.isEmpty()) {
            return getDefaultUseAliasOption();
        }

        return getInstance().useAliasOptions;
    }
}
