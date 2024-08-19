package de.espend.idea.php.annotation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import de.espend.idea.php.annotation.extension.PhpAnnotationUseAlias;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@Service
@State(name = "PhpAnnotationsPluginV2")
final public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings.State> {
    private State myState = new State();

    public static ApplicationSettings getInstance() {
        return ApplicationManager.getApplication().getService(ApplicationSettings.class);
    }

    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public static class State {
        public boolean appendRoundBracket = true;
        public List<UseAliasOption> useAliasOptions = new ArrayList<>();
        /**
         * First user change, so that can provide defaults
         */
        public boolean provideDefaults = true;
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
        State state = getInstance().getState();
        if (state == null) {
            return Collections.emptyList();
        }

        if (state.provideDefaults && state.useAliasOptions.isEmpty()) {
            return getDefaultUseAliasOption();
        }

        return state.useAliasOptions;
    }
}
