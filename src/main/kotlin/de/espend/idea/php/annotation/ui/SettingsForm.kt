package de.espend.idea.php.annotation.ui

import com.intellij.openapi.options.Configurable
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.TitledSeparator
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import de.espend.idea.php.annotation.ApplicationSettings
import de.espend.idea.php.annotation.util.PluginUtil
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class SettingsForm : Configurable {
    private val appendRoundBracket = JCheckBox("Insert round bracket after class name")
    private val buttonCleanIndex = JButton("Schedule annotation reindex")
    private val panel = createPanel()

    init {
        buttonCleanIndex.addActionListener { PluginUtil.forceReindex() }
        updateUIFromSettings()
    }

    private fun createPanel(): JPanel {
        val actionsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        actionsPanel.add(buttonCleanIndex)

        val content = FormBuilder.createFormBuilder()
            .addComponent(TitledSeparator("Actions"))
            .addComponent(actionsPanel, JBUI.scale(6))
            .addVerticalGap(JBUI.scale(12))
            .addComponent(TitledSeparator("Autocomplete (Annotations)"))
            .addComponent(appendRoundBracket, JBUI.scale(6))
            .panel

        val root = JPanel(BorderLayout())
        root.border = IdeBorderFactory.createEmptyBorder(JBUI.insets(10, 0, 0, 0))
        root.add(content, BorderLayout.NORTH)

        return root
    }

    override fun getDisplayName(): String? = null

    override fun getHelpTopic(): String? = null

    override fun createComponent(): JComponent = panel

    override fun isModified(): Boolean {
        return appendRoundBracket.isSelected != ApplicationSettings.getInstance().appendRoundBracket
    }

    override fun apply() {
        ApplicationSettings.getInstance().appendRoundBracket = appendRoundBracket.isSelected
    }

    override fun reset() {
        updateUIFromSettings()
    }

    private fun updateUIFromSettings() {
        appendRoundBracket.isSelected = ApplicationSettings.getInstance().appendRoundBracket
    }

    override fun disposeUIResources() = Unit
}
