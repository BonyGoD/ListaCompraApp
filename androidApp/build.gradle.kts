import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.google.services.firebase)
    alias(libs.plugins.firebase.crashlytics.gradle)
}

android {
    namespace = "dev.bonygod.listacompra.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.bonygod.listacompra"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 4
        versionName = "1.0.0"

        // Leer AdMob App ID desde local.properties (mismo patrón que composeApp)
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.reader())
        }
        val admobAppId = properties.getProperty("ADMOB_ANDROID_APP_ID")
            ?: throw GradleException("❌ ADMOB_ANDROID_APP_ID not found in local.properties")

        manifestPlaceholders["admobAndroidAppId"] = admobAppId
    }

    // Configuración de firma para release
    signingConfigs {
        create("release") {
            val properties = Properties()
            val localPropertiesFile = project.rootProject.file("local.properties")

            val keystoreFile = project.rootProject.file("app-keystore.jks")
            val hasKeystoreConfig = localPropertiesFile.exists() && keystoreFile.exists()

            if (hasKeystoreConfig) {
                properties.load(localPropertiesFile.reader())
                val keystorePassword = properties.getProperty("KEYSTORE_PASSWORD")
                val keyAlias = properties.getProperty("KEY_ALIAS")
                val keyPassword = properties.getProperty("KEY_PASSWORD")

                if (keystorePassword != null && keyAlias != null && keyPassword != null) {
                    storeFile = keystoreFile
                    storePassword = keystorePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                    println("✅ Release signing configured")
                }
            } else {
                println("⚠️ Keystore not found. Release build will use debug signing.")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            // Aplicar firma si está configurada, sino usar debug signing
            val releaseSigningConfig = signingConfigs.getByName("release")
            if (releaseSigningConfig.storeFile != null) {
                signingConfig = releaseSigningConfig
                println("✅ Using release signing")
            } else {
                println("⚠️ Using debug signing for release build (keystore not configured) or is iOS compile")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Habilitar símbolos de depuración nativos para Google Play
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":composeApp"))

    //Firebase
    implementation(platform(libs.firebase.bom))
}

