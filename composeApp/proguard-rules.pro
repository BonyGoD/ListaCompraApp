# ==========================================
# ProGuard Rules - ComposeApp Module
# ==========================================

# ==========================================
# Stacktraces útiles en release
# ==========================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Anotaciones (necesarias para reflection)
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

# ==========================================
# Kotlin Serialization (MODELOS)
# ==========================================
# Serializers generados
-keep,includedescriptorclasses class dev.bonygod.listacompra.**$$serializer { *; }

# Companion objects
-keepclassmembers class dev.bonygod.listacompra.** {
    *** Companion;
}

# serializer() method
-keepclasseswithmembers class dev.bonygod.listacompra.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==========================================
# Firebase / Firestore
# ==========================================
-dontwarn dev.gitlive.firebase.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firestore @PropertyName
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# ==========================================
# Koin (DI)
# ==========================================
# ViewModels inyectados por Koin
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

-dontwarn org.koin.**

# Mantener Koin para inyección de dependencias (CLIENT_ID)
-keep class org.koin.** { *; }
-keepclassmembers class org.koin.** {
    *;
}
-keep class * extends org.koin.core.module.Module { *; }

# ==========================================
# Navigation / Credentials / Ads
# ==========================================
-dontwarn androidx.navigation.**
-dontwarn com.google.android.gms.ads.**

# ==========================================
# Google Sign-In / Credentials API
# ==========================================
# Mantener TODAS las clases de Credentials API
-keep class androidx.credentials.** { *; }
-keep interface androidx.credentials.** { *; }
-keepclassmembers class androidx.credentials.** {
    public <init>(...);
    public <methods>;
}

# GoogleIdToken específico
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep interface com.google.android.libraries.identity.googleid.** { *; }
-keepclassmembers class com.google.android.libraries.identity.googleid.** {
    *;
}

# Play Services Auth
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep interface com.google.android.gms.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep interface com.google.firebase.auth.** { *; }
-keepclassmembers class com.google.firebase.auth.** {
    *;
}

# Firebase Auth internal
-keep class com.google.firebase.** { *; }
-keepclassmembers class com.google.firebase.** {
    *;
}

# ==========================================
# GoogleSignInKMP Library (Tu librería)
# ==========================================
# Mantener TODA la librería GoogleSignInKMP
-keep class dev.bonygod.googlesignin.kmp.** { *; }
-keep interface dev.bonygod.googlesignin.kmp.** { *; }
-keepclassmembers class dev.bonygod.googlesignin.kmp.** {
    *;
}

# Evitar warnings
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**


# ==========================================
# AndroidX / Compose
# ==========================================
# Compose y AndroidX ya traen sus reglas
-dontwarn androidx.compose.**
-dontwarn androidx.lifecycle.**
-dontwarn androidx.activity.**
-dontwarn androidx.core.**

# ==========================================
# Tu app (solo lo necesario)
# ==========================================
# Modelos usados por Firestore / Serialization
-keep class dev.bonygod.listacompra.**.model.** { *; }
-keep class dev.bonygod.listacompra.**.domain.model.** { *; }
-keep class dev.bonygod.listacompra.**.data.model.** { *; }

# Mantener nombres para debugging (opcional)
-keepnames class dev.bonygod.listacompra.**

# ==========================================
# Limpieza de logs (opcional)
# ==========================================
# Descomenta para eliminar logs en producción
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }
