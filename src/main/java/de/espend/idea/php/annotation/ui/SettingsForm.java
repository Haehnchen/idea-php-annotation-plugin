package de.espend.idea.php.annotation.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.util.PluginUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SettingsForm implements Configurable {
    private final JCheckBox appendRoundBracket = new JCheckBox("Insert round bracket after class name");
    private final JButton buttonCleanIndex = new JButton("Schedule annotation reindex");
    private final JPanel panel;

    public SettingsForm() {
        this.panel = createPanel();

        buttonCleanIndex.addActionListener(e -> PluginUtil.forceReindex());
        updateUIFromSettings();
    }

    private JPanel createPanel() {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionsPanel.add(buttonCleanIndex);

        JPanel content = FormBuilder.createFormBuilder()
            .addComponent(new TitledSeparator("Actions"))
            .addComponent(actionsPanel, JBUI.scale(6))
            .addVerticalGap(JBUI.scale(12))
            .addComponent(new TitledSeparator("Autocomplete (Annotations)"))
            .addComponent(appendRoundBracket, JBUI.scale(6))
            .getPanel();

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.insets(10, 0, 0, 0)));
        root.add(content, BorderLayout.NORTH);

        return root;
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
        return appendRoundBracket.isSelected() != ApplicationSettings.getInstance().appendRoundBracket;
    }

    @Override
    public void apply() {
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
