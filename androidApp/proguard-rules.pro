# ==========================================
# ProGuard Rules - AndroidApp Module
# ==========================================

# Application
-keep class dev.bonygod.listacompra.ListaCompraApp { *; }

# Main launcher Activity
-keep class dev.bonygod.listacompra.MainActivity { *; }

# ==========================================
# Kotlin - Reflexión y Coroutines
# ==========================================
-keepattributes *Annotation*, InnerClasses
-keepattributes Signature, Exception

# Kotlin Metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

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

# CustomCredential - CRÍTICO para type checking
-keep class androidx.credentials.CustomCredential { *; }
-keep class androidx.credentials.Credential { *; }
-keep class androidx.credentials.exceptions.** { *; }

# GetCredentialRequest y opciones
-keep class androidx.credentials.GetCredentialRequest { *; }
-keep class androidx.credentials.GetCredentialRequest$Builder { *; }

# GoogleIdToken específico
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep interface com.google.android.libraries.identity.googleid.** { *; }
-keepclassmembers class com.google.android.libraries.identity.googleid.** {
    *;
}

# GoogleIdTokenCredential - CRÍTICO
-keep class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential { *; }
-keepclassmembers class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential {
    public static *** TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;
    public static *** createFrom(...);
    public *** getIdToken();
    *;
}

# GetGoogleIdOption
-keep class com.google.android.libraries.identity.googleid.GetGoogleIdOption { *; }
-keep class com.google.android.libraries.identity.googleid.GetGoogleIdOption$Builder { *; }

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

# Firebase Auth GoogleAuthProvider - CRÍTICO
-keep class com.google.firebase.auth.GoogleAuthProvider { *; }
-keepclassmembers class com.google.firebase.auth.GoogleAuthProvider {
    public static *** getCredential(...);
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

# Mantener composables de GoogleSignInKMP
-keep @androidx.compose.runtime.Composable class * { *; }
-keep class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ==========================================
# Koin DI (para inyección de CLIENT_ID)
# ==========================================
-keep class org.koin.** { *; }
-keepclassmembers class org.koin.** {
    *;
}
-keep class * extends org.koin.core.module.Module { *; }

# Inyección por nombre (named("CLIENT_ID"))
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# ==========================================
# Compose Runtime
# ==========================================
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** {
    *;
}

# ==========================================
# Evitar warnings
# ==========================================
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**
-dontwarn org.koin.**
-dontwarn kotlin.**
-dontwarn kotlinx.**


# ==========================================
# Desactivar optimizaciones agresivas
# ==========================================
# No optimizar clases críticas de Google Sign-In
-keep,allowobfuscation,allowoptimization class androidx.credentials.** { *; }
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# No remover campos/métodos usados por reflexión
-keepclassmembers class * {
    @androidx.credentials.* *;
    @com.google.android.gms.* *;
}
