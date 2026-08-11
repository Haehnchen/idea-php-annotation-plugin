package de.espend.idea.php.annotation.ui

import com.intellij.openapi.options.Configurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ElementProducer
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import de.espend.idea.php.annotation.ApplicationSettings
import de.espend.idea.php.annotation.dict.UseAliasOption
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTable

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class UseAliasListForm : Configurable {
    private val tableView = TableView<UseAliasOption>()
    private val modelList = ListTableModel<UseAliasOption>(
        arrayOf(ClassColumn(), AliasColumn(), DisableColumn()),
        mutableListOf(),
    )
    private val tablePanel = JPanel(BorderLayout())
    private val buttonReset = JButton("Default reset")
    private var changed = false
    private val panel = createPanel()

    init {
        tableView.setModelAndUpdateColumns(modelList)

        buttonReset.addActionListener {
            resetList()
            changed = true
            apply()
            ApplicationSettings.getInstance().provideDefaults = false
            JOptionPane.showMessageDialog(panel, "Default alias applied")
        }

        tablePanel.add(createToolbarDecorator().createPanel(), BorderLayout.CENTER)
        initList()
    }

    private fun createPanel(): JPanel {
        val description = JLabel("Auto insert use alias for given class scope eg \"Doctrine\\ORM\\Mapping as ORM\"")

        val header = JPanel(BorderLayout(JBUI.scale(8), 0))
        header.add(description, BorderLayout.WEST)
        header.add(buttonReset, BorderLayout.EAST)

        val root = JPanel(BorderLayout(0, JBUI.scale(10)))
        root.add(header, BorderLayout.NORTH)
        root.add(tablePanel, BorderLayout.CENTER)

        return root
    }

    private fun createToolbarDecorator(): ToolbarDecorator {
        val tableDecorator = ToolbarDecorator.createDecorator(
            tableView,
            object : ElementProducer<UseAliasOption> {
                override fun createElement(): UseAliasOption? = null

                override fun canCreateElement(): Boolean = true
            },
        )

        tableDecorator.setEditAction {
            val useAliasOption = tableView.selectedObject ?: return@setEditAction
            UseAliasForm.create(panel, useAliasOption) {
                tableView.tableViewModel.fireTableDataChanged()
                changed = true
            }
        }

        tableDecorator.setAddAction {
            UseAliasForm.create(panel) { option ->
                tableView.listTableModel.addRow(option)
                changed = true
            }
        }

        tableDecorator.setRemoveAction {
            val selectedRow = tableView.selectedRow
            if (selectedRow < 0) {
                return@setRemoveAction
            }

            modelList.removeRow(selectedRow)
            tableView.tableViewModel.fireTableDataChanged()
            changed = true
        }

        tableDecorator.disableDownAction()
        tableDecorator.disableUpAction()

        return tableDecorator
    }

    private fun resetList() {
        while (modelList.rowCount > 0) {
            modelList.removeRow(0)
        }

        modelList.addRows(ApplicationSettings.getDefaultUseAliasOption())
    }

    private fun initList() {
        modelList.addRows(ApplicationSettings.getUseAliasOptionsWithDefaultFallback())
    }

    override fun getDisplayName(): String = "Use Alias"

    override fun getHelpTopic(): String? = null

    override fun createComponent(): JComponent = panel

    override fun isModified(): Boolean = changed

    override fun apply() {
        ApplicationSettings.getInstance().useAliasOptions = ArrayList(tableView.listTableModel.items)
        ApplicationSettings.getInstance().provideDefaults = false
        changed = false
    }

    override fun reset() {
        while (modelList.rowCount > 0) {
            modelList.removeRow(0)
        }

        initList()
        changed = false
    }

    override fun disposeUIResources() = Unit

    private class ClassColumn : ColumnInfo<UseAliasOption, String>("Class") {
        override fun valueOf(option: UseAliasOption): String? = option.className
    }

    private class AliasColumn : ColumnInfo<UseAliasOption, String>("Alias") {
        override fun valueOf(option: UseAliasOption): String? = option.alias
    }

    private inner class DisableColumn : ColumnInfo<UseAliasOption, Boolean>("Status") {
        override fun valueOf(option: UseAliasOption): Boolean = option.isEnabled

        override fun setValue(option: UseAliasOption, value: Boolean) {
            option.isEnabled = value
            tableView.listTableModel.fireTableDataChanged()
            changed = true
        }

        override fun getWidth(table: JTable): Int = 50

        override fun isCellEditable(option: UseAliasOption): Boolean = true

        override fun getColumnClass(): Class<*> = Boolean::class.javaObjectType
    }
}
