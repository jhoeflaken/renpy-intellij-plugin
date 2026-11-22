import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

sourceSets {
    main {
        java.srcDirs("src/main/gen")
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    testImplementation(libs.junit)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        pycharm("2026.3")
        testFramework(TestFrameworkType.Platform)

        // Add plugin dependencies for compilation here, for example:
        bundledModule("com.intellij.modules.python")
    }
}

tasks.generateLexer {
    sourceFile.set(file("src/main/grammars/RenpyLexer.flex"))
    targetOutputDir.set(file("src/main/gen/io/mayasoft/renpy/language/lexer"))
    purgeOldFiles.set(true)
}

tasks.compileKotlin {
    dependsOn(tasks.generateLexer)
}
