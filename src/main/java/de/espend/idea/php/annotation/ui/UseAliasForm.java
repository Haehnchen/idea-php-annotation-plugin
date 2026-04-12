package de.espend.idea.php.annotation.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.refactoring.PhpNameUtil;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class UseAliasForm extends DialogWrapper {

    @NotNull
    private final UseAliasOption useAliasOption;

    @NotNull
    private final Callback callback;

    private final JBTextField textClassName = new JBTextField();
    private final JBTextField textAlias = new JBTextField();
    private final JBCheckBox checkStatus = new JBCheckBox("Enabled");
    private final JPanel contentPane;

    public UseAliasForm(@Nullable Component component, @NotNull UseAliasOption useAliasOption, @NotNull Callback callback) {
        super(component, true);
        this.useAliasOption = useAliasOption;
        this.callback = callback;
        this.contentPane = createPanel();

        textClassName.setText(useAliasOption.getClassName());
        textAlias.setText(useAliasOption.getAlias());
        checkStatus.setSelected(useAliasOption.isEnabled());

        setTitle("Use Alias");
        init();
    }

    private JPanel createPanel() {
        JPanel panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Class (Doctrine\\ORM\\Mapping)", textClassName, 1, false)
            .addLabeledComponent("Alias (ORM)", textAlias, 1, false)
            .addComponent(checkStatus, JBUI.scale(8))
            .getPanel();

        panel.setBorder(JBUI.Borders.empty(8));
        panel.setPreferredSize(JBUI.size(400, panel.getPreferredSize().height));

        return panel;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return textClassName;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String classText = getNormalizedClassName();
        if (!PhpNameUtil.isValidNamespaceFullName(classText, PhpLanguageLevel.DEFAULT)) {
            return new ValidationInfo("Invalid class name", textClassName);
        }

        String alias = textAlias.getText();
        if (!PhpNameUtil.isValidNamespaceFullName(alias, PhpLanguageLevel.DEFAULT)) {
            return new ValidationInfo("Invalid alias", textAlias);
        }

        return null;
    }

    @Override
    protected void doOKAction() {
        this.useAliasOption.setClassName(getNormalizedClassName());
        this.useAliasOption.setAlias(textAlias.getText());
        this.useAliasOption.setEnabled(checkStatus.isSelected());

        this.callback.ok(this.useAliasOption);
        super.doOKAction();
    }

    @NotNull
    private String getNormalizedClassName() {
        return StringUtils.strip(textClassName.getText(), "\\");
    }

    public static void create(@NotNull Component component, @NotNull Callback callback) {
        create(component, new UseAliasOption("", "", true), callback);
    }

    public static void create(@NotNull Component component, @NotNull UseAliasOption option, @NotNull Callback callback) {
        new UseAliasForm(component, option, callback).show();
    }

    public interface Callback {
        void ok(@NotNull UseAliasOption option);
    }
}
