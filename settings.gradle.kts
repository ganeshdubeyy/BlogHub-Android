pluginManagement {
    repositories {
        // Use the standard google() repository without restrictions
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
        // You might need to add this for Crashlytics symbols depending on your setup
        // If the build fails after the first fix, add this line:
        // maven("https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "BlogHub"
include(":app")
