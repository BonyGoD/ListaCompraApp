# ==========================================
# ProGuard Rules - AndroidApp Module
# ==========================================

# Este módulo solo contiene el Application y MainActivity
# Las reglas principales están en composeApp/proguard-rules.pro

# Mantener Application class
-keep class dev.bonygod.listacompra.ListaCompraApp { *; }

# Mantener MainActivity
-keep class dev.bonygod.listacompra.MainActivity { *; }

# Firebase necesita reflexión en el Application
-keepclassmembers class dev.bonygod.listacompra.ListaCompraApp {
    <init>(...);
}
