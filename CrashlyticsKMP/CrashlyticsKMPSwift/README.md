# CrashlyticsKMPSwift

Swift Package que actúa de bridge entre Kotlin/Native y Firebase Crashlytics para la
librería [CrashlyticsKMP](../README.md).

## Instalación

### En tu proyecto Xcode:

1. File → Add Package Dependencies
2. Agrega este paquete local (carpeta `CrashlyticsKMP/`, raíz de `Package.swift`) o desde repositorio
3. Selecciona `CrashlyticsKMPSwift` como producto para el target de la app

## Uso

### En AppDelegate:

```swift
import CrashlyticsKMPSwift
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        FirebaseApp.configure()

        // Debe ir DESPUÉS de FirebaseApp.configure()
        _ = CrashlyticsCallbackHelper.shared

        return true
    }
}
```

Este paquete **no** llama a `FirebaseApp.configure()`: es responsabilidad de la app.

⚠️ **Orden de arranque, crítico:** `NSNotificationCenter` no encola nada. Cualquier
notificación posteada desde Kotlin (`CrashlyticsKMP.initialize()` incluido) antes de que
se ejecute `_ = CrashlyticsCallbackHelper.shared` se pierde en silencio, sin ningún aviso.
Por eso `CrashlyticsKMP.initialize()` debe llamarse **después** de esa línea — si se llama
antes, el `setCollectionEnabled` inicial y las custom keys base de `initialize()` no llegan,
y Crashlytics se queda con su configuración por defecto.

## Arquitectura

Bridge unidireccional Kotlin → Swift, fire-and-forget (no hay notificaciones de vuelta
Swift → Kotlin en esta versión):

- **Kotlin** (`IosCrashReporter`, módulo `crashlytics-kmp`) postea notificaciones vía
  `NSNotificationCenter` con los datos ya convertidos a `String`.
- **`CrashlyticsCallbackHelper`** (singleton) escucha esas notificaciones, valida el
  `userInfo` y delega en `CrashlyticsBridge`.
- **`CrashlyticsBridge`** ejecuta las llamadas reales sobre `Crashlytics.crashlytics()`.

### Notificaciones soportadas (Kotlin → Swift):

| Notificación | `userInfo` |
|---|---|
| `CrashlyticsKMPLog` | `message: String` |
| `CrashlyticsKMPSetUserId` | `userId: String` (`""` ⇒ limpiar) |
| `CrashlyticsKMPSetCustomKey` | `key: String`, `value: String`, `type: "string"\|"bool"\|"int"\|"long"\|"double"` |
| `CrashlyticsKMPSetCustomKeys` | `keys: [String: String]` |
| `CrashlyticsKMPRecordError` | `name: String`, `reason: String`, `stackTrace: [String]`, `keys: [String: String]` |
| `CrashlyticsKMPSetCollectionEnabled` | `enabled: String` (`"true"`/`"false"`) |
| `CrashlyticsKMPSendUnsentReports` | — |
| `CrashlyticsKMPDeleteUnsentReports` | — |
| `CrashlyticsKMPForceCrash` | — |

## Componentes

### CrashlyticsCallbackHelper
Singleton que escucha las notificaciones desde Kotlin y coordina las llamadas al bridge.

### CrashlyticsBridge
Envuelve `Crashlytics.crashlytics()`: reconstruye tipos, aplica custom keys y registra
excepciones no fatales.

### KotlinStackTraceMapper
Normaliza los frames de stack trace de Kotlin/Native (`kfun:paquete.Clase#metodo(){}`)
a símbolos legibles para `ExceptionModel.stackTrace`.

## Limitaciones conocidas

- Crashlytics iOS no soporta custom keys por-reporte: las `keys` de `recordError` se
  aplican como custom keys globales, igual que en la implementación de Android.
- No enviar PII (email, nombre real) como custom key ni como `userId`. Solo el `uid` de
  Firebase.

## Licencia

Ver [`LICENSE.md`](../LICENSE.md) de `CrashlyticsKMP/`.
