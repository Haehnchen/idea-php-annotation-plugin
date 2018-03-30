package de.espend.idea.php.annotation.ui;

import com.jetbrains.php.refactoring.PhpNameUtil;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class UseAliasForm extends JDialog {

    @NotNull
    private final UseAliasOption useAliasOption;

    @NotNull
    private final Callback callback;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textClassName;
    private JTextField textAlias;
    private JCheckBox checkStatus;

    public UseAliasForm(@NotNull UseAliasOption useAliasOption, @NotNull Callback callback) {
        this.useAliasOption = useAliasOption;
        this.callback = callback;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        textClassName.setText(useAliasOption.getClassName());
        textAlias.setText(useAliasOption.getAlias());
        checkStatus.setSelected(useAliasOption.isEnabled());

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String classText = StringUtils.strip(textClassName.getText(), "\\");
        if(!PhpNameUtil.isValidNamespaceFullName(classText)) {
            JOptionPane.showMessageDialog(this, "Invalid class name");
            return;
        }

        String alias = textAlias.getText();
        if(!PhpNameUtil.isValidNamespaceFullName(alias)) {
            JOptionPane.showMessageDialog(this, "Invalid alias");
            return;
        }

        this.useAliasOption.setClassName(classText);
        this.useAliasOption.setAlias(alias);
        this.useAliasOption.setEnabled(checkStatus.isSelected());

        this.callback.ok(this.useAliasOption);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void create(@NotNull Component component, @NotNull Callback callback) {
        create(component, new UseAliasOption("", "", true), callback);
    }

    public static void create(@NotNull Component component, @NotNull UseAliasOption option, @NotNull Callback callback) {
        UseAliasForm dialog = new UseAliasForm(option, callback);
        dialog.setMinimumSize(new Dimension(400, 0));
        dialog.pack();
        dialog.setTitle("Use Alias");
        dialog.setLocationRelativeTo(component);
        dialog.setVisible(true);
    }

    public interface Callback {
        void ok(@NotNull UseAliasOption option);
    }
}
