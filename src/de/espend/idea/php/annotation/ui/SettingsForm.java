package de.espend.idea.php.annotation.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import de.espend.idea.php.annotation.ApplicationSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SettingsForm implements Configurable {
    private JCheckBox appendRoundBracket;
    private JPanel panel;

    @Nls
    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        return
            !appendRoundBracket.isSelected() == ApplicationSettings.getInstance().appendRoundBracket
        ;
    }

    @Override
    public void apply() throws ConfigurationException {
        ApplicationSettings.getInstance().appendRoundBracket = appendRoundBracket.isSelected();
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    private void updateUIFromSettings() {
        appendRoundBracket.setSelected(ApplicationSettings.getInstance().appendRoundBracket);
    }

    @Override
    public void disposeUIResources() {

    }
}
