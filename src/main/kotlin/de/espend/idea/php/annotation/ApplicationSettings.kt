package de.espend.idea.php.annotation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import de.espend.idea.php.annotation.dict.UseAliasOption
import de.espend.idea.php.annotation.util.AnnotationUtil

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@State(name = "PhpAnnotationsPlugin", storages = [Storage("PhpAnnotationsPlugin.xml")])
class ApplicationSettings : PersistentStateComponent<ApplicationSettings> {
    @JvmField
    var appendRoundBracket = true

    @JvmField
    var useAliasOptions: MutableList<UseAliasOption> = ArrayList()

    /**
     * First user change, so that defaults can be provided.
     */
    @JvmField
    var provideDefaults = true

    override fun getState(): ApplicationSettings = this

    override fun loadState(state: ApplicationSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        fun getInstance(): ApplicationSettings =
            ApplicationManager.getApplication().getService(ApplicationSettings::class.java)

        @JvmStatic
        fun getDefaultUseAliasOption(): Collection<UseAliasOption> {
            val options = ArrayList<UseAliasOption>()
            options.add(UseAliasOption("Symfony\\Component\\Validator\\Constraints", "Assert", true))
            options.add(UseAliasOption("Doctrine\\ORM\\Mapping", "ORM", true))
            options.add(UseAliasOption("JMS\\DiExtraBundle\\Annotation", "DI", true))
            options.add(UseAliasOption("JMS\\Serializer\\Annotation", "Serializer", true))
            options.add(UseAliasOption("Gedmo\\Mapping\\Annotation", "Gedmo", true))
            options.add(UseAliasOption("Vich\\UploaderBundle\\Mapping\\Annotation", "Vich", true))
            options.add(UseAliasOption("FOS\\RestBundle\\Controller\\Annotations", "Rest", true))
            options.add(UseAliasOption("Swagger\\Annotations", "SWG", true))
            options.add(UseAliasOption("OpenApi\\Annotations", "OA", true))
            options.add(UseAliasOption("OpenApi\\Attributes", "OA", true))
            options.add(UseAliasOption("Sunrise\\Http\\Router\\Annotation", "Routing", true))
            options.add(UseAliasOption("Sunrise\\Symfony\\OpenApi\\Annotation", "OpenApi", true))

            for (extension in AnnotationUtil.EP_USE_ALIASES.extensions) {
                options.addAll(
                    extension.aliases.map { (alias, className) ->
                        UseAliasOption(className, alias, true)
                    },
                )
            }

            return options
        }

        @JvmStatic
        fun getUseAliasOptionsWithDefaultFallback(): Collection<UseAliasOption> {
            val settings = getInstance()
            if (settings.provideDefaults && settings.useAliasOptions.isEmpty()) {
                return getDefaultUseAliasOption()
            }

            return settings.useAliasOptions
        }
    }
}
