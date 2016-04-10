package de.espend.idea.php.annotation.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ElementProducer;
import com.intellij.util.ui.ListTableModel;
import de.espend.idea.php.annotation.ApplicationSettings;
import de.espend.idea.php.annotation.dict.UseAliasOption;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class UseAliasListForm implements Configurable {

    private ListTableModel<UseAliasOption> modelList;
    private TableView<UseAliasOption> tableView;
    private boolean changed = false;

    private JPanel panel1;
    private JPanel panel;
    private JButton buttonReset;

    public UseAliasListForm() {
        this.tableView = new TableView<UseAliasOption>();

        this.modelList = new ListTableModel<UseAliasOption>(
            new ClassColumn(),
            new AliasColumn(),
            new DisableColumn()
        );

        this.tableView.setModelAndUpdateColumns(this.modelList);

        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableView.getTableViewModel().fireTableDataChanged();
                changed = true;
                resetList();
            }
        });

        initList();
    }

    private void resetList() {
        while(this.modelList.getRowCount() > 0) {
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
        ToolbarDecorator tablePanel = ToolbarDecorator.createDecorator(this.tableView, new ElementProducer<UseAliasOption>() {
            @Override
            public UseAliasOption createElement() {
                return null;
            }

            @Override
            public boolean canCreateElement() {
                return true;
            }
        });

        tablePanel.setEditAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {

                UseAliasOption useAliasOption = tableView.getSelectedObject();
                if(useAliasOption == null) {
                    return;
                }

                UseAliasForm.create(panel1, useAliasOption, new UseAliasForm.Callback() {
                    @Override
                    public void ok(@NotNull UseAliasOption option) {
                        tableView.getTableViewModel().fireTableDataChanged();
                        changed = true;
                    }
                });
            }
        });

        tablePanel.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                UseAliasForm.create(panel1, new UseAliasForm.Callback() {
                    @Override
                    public void ok(@NotNull UseAliasOption option) {
                        tableView.getListTableModel().addRow(option);
                        changed = true;
                    }
                });
            }
        });

        tablePanel.setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                modelList.removeRow(tableView.getSelectedRow());
                tableView.getTableViewModel().fireTableDataChanged();
                changed = true;
            }
        });

        tablePanel.disableDownAction();
        tablePanel.disableUpAction();

        this.panel1.add(tablePanel.createPanel());

        return this.panel;
    }

    @Override
    public boolean isModified() {
        return this.changed;
    }

    @Override
    public void apply() throws ConfigurationException {
        List<UseAliasOption> options = new ArrayList<UseAliasOption>();

        for(UseAliasOption option :this.tableView.getListTableModel().getItems()) {
            options.add(option);
        }

        ApplicationSettings.getInstance().useAliasOptions = options;
        this.changed = false;
    }

    @Override
    public void reset() {
        while(this.modelList.getRowCount() > 0) {
            this.modelList.removeRow(0);
        }

        initList();
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

        public DisableColumn() {
            super("Status");
        }

        public Boolean valueOf(UseAliasOption twigPath) {
            return twigPath.isEnabled();
        }

        public void setValue(UseAliasOption twigPath, Boolean value){
            twigPath.setEnabled(value);
            tableView.getListTableModel().fireTableDataChanged();
            changed = true;
        }

        public int getWidth(JTable table) {
            return 50;
        }

        public boolean isCellEditable(UseAliasOption groupItem)
        {
            return true;
        }

        public Class getColumnClass()
        {
            return Boolean.class;
        }
    }
}
