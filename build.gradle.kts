import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.jetbrains.intellij.platform") version "2.10.4"
    id("org.jetbrains.changelog") version "1.3.1"
    id("org.jetbrains.qodana") version "0.1.13"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val version = providers.gradleProperty("platformVersion")
        val type = providers.gradleProperty("platformType")
        create(type, version) {
            useInstaller = false
            useCache = true
        }

        bundledPlugins("com.intellij.java", "com.jetbrains.plugins.webDeployment")

        compatiblePlugins(
            "com.jetbrains.php",
            "com.jetbrains.twig",
            "com.intellij.modules.json",
            "de.espend.idea.php.toolbox"
        )
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellijPlatform {
    val version = providers.gradleProperty("platformVersion")
    val type = providers.gradleProperty("platformType")

    pluginConfiguration {
        name = properties("pluginName")
    }

    pluginVerification {
        ides {
            create(type, version) {
                useInstaller = false
                useCache = true
            }
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(it))
            }
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
        changeNotes.set(file("src/main/resources/META-INF/change-notes.html").readText().replace("<html>", "").replace("</html>", ""))
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
         token.set(System.getenv("PUBLISH_TOKEN"))
    }

    test {
        // Support "setUp" like "BasePlatformTestCase::setUp" as valid test structure
        useJUnitPlatform {
            includeEngines("junit-vintage", "junit-jupiter")
        }
    }
}
