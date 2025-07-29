pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()          // ✅ Good: needed for Retrofit, etc.
        gradlePluginPortal()    // ✅ Good: needed for plugins
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()          // ✅ Good: needed for Retrofit, Gson, etc.
    }
}
rootProject.name = "SplitMoney"
include(":app")

 