package de.espend.idea.php.annotation.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.util.PluginUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SettingsForm implements Configurable {
    private JCheckBox appendRoundBracket;
    private JCheckBox activateBracketHighlighting;
    private JPanel panel;
    private JButton buttonCleanIndex;

    public SettingsForm() {
        buttonCleanIndex.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PluginUtil.forceReindex();
                super.mouseClicked(e);
            }
        });
    }

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
        ApplicationSettings settings = ApplicationSettings.getInstance();
        return
            !appendRoundBracket.isSelected() == settings.appendRoundBracket ||
                    !activateBracketHighlighting.isSelected() == settings.activateBracketHighlighting;
    }

    @Override
    public void apply() throws ConfigurationException {
        ApplicationSettings settings = ApplicationSettings.getInstance();
        settings.appendRoundBracket = appendRoundBracket.isSelected();
        settings.activateBracketHighlighting = activateBracketHighlighting.isSelected();
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    private void updateUIFromSettings() {
        ApplicationSettings settings = ApplicationSettings.getInstance();
        appendRoundBracket.setSelected(settings.appendRoundBracket);
        activateBracketHighlighting.setSelected(settings.activateBracketHighlighting);
    }

    @Override
    public void disposeUIResources() {

    }
}
