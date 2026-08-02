import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

kotlin {
    explicitApi()

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    val iosTargets = listOf(iosArm64(), iosSimulatorArm64(), iosX64())
    if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
        iosTargets.forEach {
            it.binaries.framework {
                baseName = "CrashlyticsKMP"
                isStatic = true
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // nada
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            api(project.dependencies.platform(libs.firebase.bom))
            api(libs.firebase.crashlytics) // api, no implementation: el consumidor lo necesita
        }
    }
}

android {
    namespace = "dev.bonygod.crashlytics.kmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

group = "com.github.BonyGoD"
version = "1.0.0"
