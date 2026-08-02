rootProject.name = "ListaCompra"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
include(":androidApp")

// Montaje temporal (Plan B de la FASE 6.1 de CRASHLYTICS_KMP_PLAN.md): el composite build
// (includeBuild + dependencySubstitution) no se estaba aplicando y Gradle intentaba resolver
// com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp contra JitPack, donde el repo aún no existe
// (la publicación es la Fase 9). Mientras tanto, el módulo se incluye directamente como proyecto
// del build de la app. En la Fase 9 esto se revierte: quitar este include/projectDir y volver a
// consumir libs.bonygod.crashlyticskmp vía JitPack.
include(":crashlytics-kmp")
project(":crashlytics-kmp").projectDir = file("CrashlyticsKMP/crashlytics-kmp")