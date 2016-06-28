package de.espend.idea.php.annotation;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@State(
    name = "EspendPhpAnnotationSetting",
    storages = {
        @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/espend_php_annotation.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class Settings implements PersistentStateComponent<Settings> {

    public static Settings getInstance(Project project) {
        return ServiceManager.getService(project, Settings.class);
    }

    @Nullable
    @Override
    public Settings getState() {
        return this;
    }

    @Override
    public void loadState(Settings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }
}
