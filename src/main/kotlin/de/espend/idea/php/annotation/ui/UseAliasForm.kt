package de.espend.idea.php.annotation.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.refactoring.PhpNameUtil
import de.espend.idea.php.annotation.dict.UseAliasOption
import org.apache.commons.lang3.StringUtils
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class UseAliasForm(
    component: Component?,
    private val useAliasOption: UseAliasOption,
    private val callback: Callback,
) : DialogWrapper(component ?: JPanel(), true) {
    private val textClassName = JBTextField()
    private val textAlias = JBTextField()
    private val checkStatus = JBCheckBox("Enabled")
    private val contentPane = createPanel()

    init {
        textClassName.text = useAliasOption.className
        textAlias.text = useAliasOption.alias
        checkStatus.isSelected = useAliasOption.isEnabled

        title = "Use Alias"
        init()
    }

    private fun createPanel(): JPanel {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Class (Doctrine\\ORM\\Mapping)", textClassName, 1, false)
            .addLabeledComponent("Alias (ORM)", textAlias, 1, false)
            .addComponent(checkStatus, JBUI.scale(8))
            .panel

        panel.border = JBUI.Borders.empty(8)
        panel.preferredSize = JBUI.size(400, panel.preferredSize.height)
        return panel
    }

    override fun createCenterPanel(): JComponent = contentPane

    override fun getPreferredFocusedComponent(): JComponent = textClassName

    override fun doValidate(): ValidationInfo? {
        if (!PhpNameUtil.isValidNamespaceFullName(normalizedClassName, PhpLanguageLevel.DEFAULT)) {
            return ValidationInfo("Invalid class name", textClassName)
        }

        if (!PhpNameUtil.isValidNamespaceFullName(textAlias.text, PhpLanguageLevel.DEFAULT)) {
            return ValidationInfo("Invalid alias", textAlias)
        }

        return null
    }

    override fun doOKAction() {
        useAliasOption.className = normalizedClassName
        useAliasOption.alias = textAlias.text
        useAliasOption.isEnabled = checkStatus.isSelected

        callback.ok(useAliasOption)
        super.doOKAction()
    }

    private val normalizedClassName: String
        get() = StringUtils.strip(textClassName.text, "\\")

    fun interface Callback {
        fun ok(option: UseAliasOption)
    }

    companion object {
        fun create(component: Component, callback: Callback) {
            create(component, UseAliasOption("", "", true), callback)
        }

        fun create(component: Component, option: UseAliasOption, callback: Callback) {
            UseAliasForm(component, option, callback).show()
        }
    }
}
