import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://jitpack.io") {
            content { includeGroup("com.github.mgsx-dev.gdx-gltf") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "RaidManager"
include(":core", ":android", ":lwjgl3")
