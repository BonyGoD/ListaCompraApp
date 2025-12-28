import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    //alias(libs.plugins.composeHotReload)
    alias(libs.plugins.google.services.firebase)
    alias(libs.plugins.firebase.crashlytics.gradle)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
                binaryOption("bundleId", "dev.bonygod.listacompra.ComposeApp")
            }
        }
    }

    jvm()

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            //Dependency Injection
            implementation(libs.koin.android)

            //Firebase
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // androidx.lifecycle.viewmodelCompose causa conflictos en iOS
            //implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlin.serialization)
            implementation(libs.kotlinx.datetime)

            //Dependency Injection
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            //GitLive Firebase
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.crashlytics)
            implementation(libs.gitlive.firebase.analitics)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            //Dependency Injection for JVM
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
    }
}

android {
    namespace = "dev.bonygod.listacompra"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.bonygod.listacompra"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "2.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "dev.bonygod.listacompra.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.bonygod.listacompra"
            packageVersion = "2.0.0"
        }
    }
}
