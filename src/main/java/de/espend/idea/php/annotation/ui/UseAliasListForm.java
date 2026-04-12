package de.espend.idea.php.annotation.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ElementProducer;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class UseAliasListForm implements Configurable {

    private final ListTableModel<UseAliasOption> modelList;
    private final TableView<UseAliasOption> tableView;
    private final JPanel panel;
    private final JPanel tablePanel = new JPanel(new BorderLayout());
    private final JButton buttonReset = new JButton("Default reset");
    private boolean changed = false;

    public UseAliasListForm() {
        this.tableView = new TableView<>();
        this.modelList = new ListTableModel<>(
            new ClassColumn(),
            new AliasColumn(),
            new DisableColumn()
        );

        this.tableView.setModelAndUpdateColumns(this.modelList);
        this.panel = createPanel();

        buttonReset.addActionListener(e -> {
            resetList();
            changed = true;
            apply();
            ApplicationSettings.getInstance().provideDefaults = false;
            JOptionPane.showMessageDialog(panel, "Default alias applied");
        });

        tablePanel.add(createToolbarDecorator().createPanel(), BorderLayout.CENTER);
        initList();
    }

    private JPanel createPanel() {
        JLabel description = new JLabel("Auto insert use alias for given class scope eg \"Doctrine\\ORM\\Mapping as ORM\"");

        JPanel header = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        header.add(description, BorderLayout.WEST);
        header.add(buttonReset, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout(0, JBUI.scale(10)));
        root.add(header, BorderLayout.NORTH);
        root.add(tablePanel, BorderLayout.CENTER);

        return root;
    }

    private ToolbarDecorator createToolbarDecorator() {
        ToolbarDecorator tableDecorator = ToolbarDecorator.createDecorator(this.tableView, new ElementProducer<>() {
            @Override
            public UseAliasOption createElement() {
                return null;
            }

            @Override
            public boolean canCreateElement() {
                return true;
            }
        });

        tableDecorator.setEditAction(anActionButton -> {
            UseAliasOption useAliasOption = tableView.getSelectedObject();
            if (useAliasOption == null) {
                return;
            }

            UseAliasForm.create(panel, useAliasOption, option -> {
                tableView.getTableViewModel().fireTableDataChanged();
                changed = true;
            });
        });

        tableDecorator.setAddAction(anActionButton -> UseAliasForm.create(panel, option -> {
            tableView.getListTableModel().addRow(option);
            changed = true;
        }));

        tableDecorator.setRemoveAction(anActionButton -> {
            int selectedRow = tableView.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }

            modelList.removeRow(selectedRow);
            tableView.getTableViewModel().fireTableDataChanged();
            changed = true;
        });

        tableDecorator.disableDownAction();
        tableDecorator.disableUpAction();

        return tableDecorator;
    }

    private void resetList() {
        while (this.modelList.getRowCount() > 0) {
            this.modelList.removeRow(0);
        }

        this.modelList.addRows(ApplicationSettings.getDefaultUseAliasOption());
    }

    private void initList() {
        this.modelList.addRows(ApplicationSettings.getUseAliasOptionsWithDefaultFallback());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Use Alias";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.panel;
    }

    @Override
    public boolean isModified() {
        return this.changed;
    }

    @Override
    public void apply() {
        ApplicationSettings.getInstance().useAliasOptions = new ArrayList<>(this.tableView.getListTableModel().getItems());
        ApplicationSettings.getInstance().provideDefaults = false;
        this.changed = false;
    }

    @Override
    public void reset() {
        while (this.modelList.getRowCount() > 0) {
            this.modelList.removeRow(0);
        }

        initList();
        this.changed = false;
    }

    @Override
    public void disposeUIResources() {

    }

    private static class ClassColumn extends ColumnInfo<UseAliasOption, String> {

        ClassColumn() {
            super("Class");
        }

        @Nullable
        @Override
        public String valueOf(UseAliasOption option) {
            return option.getClassName();
        }
    }

    private static class AliasColumn extends ColumnInfo<UseAliasOption, String> {

        AliasColumn() {
            super("Alias");
        }

        @Nullable
        @Override
        public String valueOf(UseAliasOption option) {
            return option.getAlias();
        }
    }

    private class DisableColumn extends ColumnInfo<UseAliasOption, Boolean> {

        DisableColumn() {
            super("Status");
        }

        @Override
        public Boolean valueOf(UseAliasOption twigPath) {
            return twigPath.isEnabled();
        }

        @Override
        public void setValue(UseAliasOption twigPath, Boolean value) {
            twigPath.setEnabled(value);
            tableView.getListTableModel().fireTableDataChanged();
            changed = true;
        }

        @Override
        public int getWidth(JTable table) {
            return 50;
        }

        @Override
        public boolean isCellEditable(UseAliasOption groupItem) {
            return true;
        }

        @Override
        public Class<?> getColumnClass() {
            return Boolean.class;
        }
    }
}
