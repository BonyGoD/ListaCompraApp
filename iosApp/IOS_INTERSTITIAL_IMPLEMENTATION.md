# Implementación de Precarga de Anuncios Intersticiales en iOS

## ✅ Estado Actual: COMPLETADO

### ✅ Implementado:
1. **AdPreloader.swift** - Singleton que gestiona la precarga de intersticiales
2. **AdPreloader.swift (Notificaciones)** - Sistema de notificaciones para bridge Kotlin ↔ Swift
3. **iOSApp.swift** - Precarga el anuncio al iniciar la app
4. **InterstitialAdHelper.ios.kt** - Bridge completo usando NSNotificationCenter

### Estructura de Archivos:
```
iosApp/iosApp/
├── AdPreloader.swift          ← Gestor + Sistema de Notificaciones (✅ COMPLETO)
├── AdPreloaderBridge.swift    ← Bridge Obj-C (NO NECESARIO)
├── iOSApp.swift               ← Precarga al inicio (✅ COMPLETO)
└── ...

composeApp/src/iosMain/
└── kotlin/.../InterstitialAdHelper.ios.kt  ← Bridge via Notificaciones (✅ COMPLETO)
```

## 🎯 Solución Implementada: Sistema de Notificaciones

Se implementó usando **NSNotificationCenter** para comunicación bidireccional entre Kotlin y Swift.

### Notificaciones Disponibles:

#### Desde Kotlin → Swift:
- `AdPreloaderPreloadRequested` - Solicita precargar un anuncio
  - userInfo: `["adUnitId": String]`
- `AdPreloaderShowRequested` - Solicita mostrar el anuncio precargado
- `AdPreloaderIsReadyRequested` - Consulta si hay anuncio listo

#### Desde Swift → Kotlin:
- `AdPreloaderAdShown` - El anuncio se mostró
- `AdPreloaderAdDismissed` - El usuario cerró el anuncio
- `AdPreloaderShowFailed` - Error al mostrar
  - userInfo: `["error": String]`

## 🔧 Funcionamiento

### 1. Al iniciar la app (iOSApp.swift):
```swift
AdPreloader.shared.preloadAd(adUnitId: "ca-app-pub-...")
```

### 2. Al hacer login/registro (Kotlin):
```kotlin
// Enviar notificación
NSNotificationCenter.defaultCenter.postNotificationName(
    "AdPreloaderShowRequested", 
    object = null
)

// Escuchar respuestas
NSNotificationCenter.defaultCenter.addObserverForName(
    name = "AdPreloaderAdDismissed",
    ...
)
```

### 3. En Swift (AdPreloader):
```swift
@objc private func handleShowRequest(_ notification: Notification) {
    showAd(from: rootVC, ...) { 
        // Enviar notificación de vuelta a Kotlin
        NotificationCenter.default.post(
            name: NSNotification.Name("AdPreloaderAdDismissed"),
            ...
        )
    }
}
```

## 📊 Flujo Completo

```
1. App inicia → AdPreloader.shared.preloadAd()
2. [2-3 segundos después] → Anuncio precargado ✅
3. Usuario hace login
4. Kotlin envía: "AdPreloaderShowRequested"
5. Swift recibe y muestra el anuncio INSTANTÁNEAMENTE
6. Usuario cierra el anuncio
7. Swift envía: "AdPreloaderAdDismissed"
8. Kotlin recibe y navega a Home
9. Kotlin envía: "AdPreloaderPreloadRequested" (recarga)
```

## ✅ Ventajas de Esta Solución

- ✅ **Sin configuración adicional de build**
- ✅ **Patrón ya usado en el proyecto** (AdMobCallbackHelper)
- ✅ **Comunicación bidireccional**
- ✅ **Funciona de forma consistente**
- ✅ **Fácil de debuggear** (logs claros en ambos lados)

## 🗑️ Archivos No Necesarios

- ❌ `AdPreloaderBridge.swift` - No se usa (quedó de pruebas anteriores)

## 📝 Logs Esperados

```
// En Swift:
🟡 [AdPreloader] Preloading interstitial ad: ca-app-pub-...
✅ [AdPreloader] Interstitial ad preloaded successfully
🔵 [AdPreloader] Show request received
🟢 [AdPreloader] Showing preloaded interstitial ad
✅ [AdPreloader] Ad presented full screen content
👋 [AdPreloader] Ad dismissed

// En Kotlin:
🔵 [iOS-Kotlin] Requesting to show ad
✅ [iOS-Kotlin] Ad shown
👋 [iOS-Kotlin] Ad dismissed
```

## 🎉 Resultado Final

**iOS está 100% funcional con precarga de anuncios intersticiales.**

- ✅ Android: Precarga funcional
- ✅ iOS: Precarga funcional
- ✅ Sin esperas para el usuario
- ✅ Recarga automática después de mostrar

---

**Nota**: El archivo `AdPreloaderBridge.swift` puede eliminarse si lo deseas, ya que no se está usando. La solución final usa únicamente el sistema de notificaciones de iOS.
