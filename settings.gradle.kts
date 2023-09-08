pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Musica"
include(":app")
include(":core:model")
include(":core:store")
include(":core:playback")
include(":core:ui")
include(":feature:songs")
include(":feature:playlists")
