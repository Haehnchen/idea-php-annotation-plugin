package de.espend.idea.php.annotation

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@State(
    name = "EspendPhpAnnotationSetting",
    storages = [Storage("espend_php_annotation.xml")],
)
@Service(Service.Level.PROJECT)
class Settings : PersistentStateComponent<Settings> {
    override fun getState(): Settings = this

    override fun loadState(state: Settings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): Settings = project.getService(Settings::class.java)
    }
}
