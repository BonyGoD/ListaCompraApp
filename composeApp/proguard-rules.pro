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

# ==========================================
# Navigation / Credentials / Ads
# ==========================================
-dontwarn androidx.navigation.**
-dontwarn androidx.credentials.**
-dontwarn com.google.android.gms.ads.**

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
