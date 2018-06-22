package de.espend.idea.php.annotation;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@State(
        name = "EspendPhpAnnotationSetting",
        storages = {
                @Storage(file = StoragePathMacros.WORKSPACE_FILE),
                @Storage(file = StoragePathMacros.MODULE_FILE + "/espend_php_annotation.xml", scheme = StorageScheme.DIRECTORY_BASED)
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
    public void loadState(@NotNull Settings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }
}
