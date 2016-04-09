package de.espend.idea.php.annotation;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@State(name = "PhpAnnotationsPlugin", storages = @Storage(id = "php-annotations-plugin", file = "$APP_CONFIG$/php-annotations-plugin.app.xml"))
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings> {

    public boolean appendRoundBracket = true;

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
}
