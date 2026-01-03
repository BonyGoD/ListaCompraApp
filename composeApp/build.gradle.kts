import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.gradleBuildConfig)
}

kotlin {

    // ---------- ANDROID ----------
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // ---------- iOS TARGETS (SIEMPRE DEFINIDOS) ----------
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // ---------- iOS FRAMEWORK SOLO EN macOS ----------
    if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
        targets.withType<KotlinNativeTarget>().configureEach {
            binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
                binaryOption(
                    "bundleId",
                    "dev.bonygod.listacompra.ComposeApp"
                )
            }
        }
    }

    // ---------- SOURCE SETS ----------
    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlin.serialization)
                implementation(libs.kotlinx.datetime)

                // Dependency Injection
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                // GitLive Firebase
                implementation(libs.gitlive.firebase.firestore)
                implementation(libs.gitlive.firebase.auth)
                implementation(libs.gitlive.firebase.crashlytics)
                implementation(libs.gitlive.firebase.analitics)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)

                // Dependency Injection
                implementation(libs.koin.android)

                // Firebase
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.auth)

                // Sign In with Google
                implementation(libs.androidx.credentials)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.googleid)
                implementation(libs.play.services.auth)
            }
        }

        // ---------- iOS SHARED ----------
        val iosMain by creating {
            dependsOn(commonMain)
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

// ---------- ANDROID CONFIG ----------
android {
    namespace = "dev.bonygod.listacompra"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// ---------- BUILDCONFIG ----------
buildConfig {
    packageName("dev.bonygod.listacompra")

    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").reader())
    val apiKey = properties.getProperty("FIREBASE_API_KEY")
    val clientId = properties.getProperty("CLIENT_ID")

    buildConfigField("FIREBASE_API_KEY", apiKey)
    buildConfigField("CLIENT_ID", clientId)
}

// ---------- DEBUG ----------
dependencies {
    debugImplementation(compose.uiTooling)
}
