package de.espend.idea.php.annotation.ui;

import com.intellij.openapi.options.Configurable;
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
        return
            !appendRoundBracket.isSelected() == ApplicationSettings.getInstance().getState().appendRoundBracket
        ;
    }

    @Override
    public void apply() {
        ApplicationSettings.getInstance().getState().appendRoundBracket = appendRoundBracket.isSelected();
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    private void updateUIFromSettings() {
        appendRoundBracket.setSelected(ApplicationSettings.getInstance().getState().appendRoundBracket);
    }

    @Override
    public void disposeUIResources() {

    }
}
