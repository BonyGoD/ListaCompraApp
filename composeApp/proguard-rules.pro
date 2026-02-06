# ==========================================
# ProGuard Rules - ListaCompra App
# ==========================================

# Mantener información de líneas para stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Mantener anotaciones y clases internas
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

# ==========================================
# Kotlin & Coroutines
# ==========================================
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.bonygod.listacompra.**$$serializer { *; }
-keepclassmembers class dev.bonygod.listacompra.** {
    *** Companion;
}
-keepclasseswithmembers class dev.bonygod.listacompra.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==========================================
# Jetpack Compose
# ==========================================
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-dontwarn androidx.compose.**

# ==========================================
# Firebase (GitLive)
# ==========================================
-keep class dev.gitlive.firebase.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn dev.gitlive.firebase.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# ==========================================
# Koin (Dependency Injection)
# ==========================================
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.dsl.** { *; }
-keepnames class androidx.lifecycle.ViewModel
-keepclassmembers public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# ==========================================
# Navigation 3
# ==========================================
-keep class androidx.navigation.** { *; }
-keep class androidx.navigation3.** { *; }
-dontwarn androidx.navigation.**

# ==========================================
# Google Sign-In & Credentials
# ==========================================
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn androidx.credentials.**

# ==========================================
# Google AdMob
# ==========================================
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.mediation.** { *; }
-dontwarn com.google.android.gms.ads.**

# ==========================================
# AndroidX & Lifecycle
# ==========================================
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.core.** { *; }
-dontwarn androidx.lifecycle.**

# ==========================================
# Tu app - Solo mantener lo necesario
# ==========================================
# ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Data classes y modelos (si usas Firestore)
-keep class dev.bonygod.listacompra.**.model.** { *; }
-keep class dev.bonygod.listacompra.**.domain.model.** { *; }
-keep class dev.bonygod.listacompra.**.data.model.** { *; }

# Clases de UI (Compose)
-keep class dev.bonygod.listacompra.**.ui.** { *; }

# Mantener nombres de clases para debugging
-keepnames class dev.bonygod.listacompra.**

# ==========================================
# Optimizaciones
# ==========================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Remover logs en producción (opcional - descomenta si quieres)
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }
