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
        versionCode = 1
        versionName = "1.0.0"

        // Leer AdMob App ID desde local.properties (mismo patrón que composeApp)
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.reader())
        }
        val admobAppId = properties.getProperty("ADMOB_ANDROID_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"

        manifestPlaceholders["admobAndroidAppId"] = admobAppId
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
    implementation(project(":composeApp"))

    //Firebase
    implementation(platform(libs.firebase.bom))
}

