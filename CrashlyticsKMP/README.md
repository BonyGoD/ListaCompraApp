# CrashlyticsKMP

Librería Kotlin Multiplatform (Android + iOS) para crash reporting sobre Firebase Crashlytics, con
una única API Kotlin común (`CrashReporter`) para ambas plataformas.

## 🏗️ Arquitectura

```
                       ┌──────────────────────────────────────────┐
  App (commonMain) ──▶ │  CrashlyticsKMP.initialize(config)        │
                       │  CrashlyticsKMP.reporter : CrashReporter  │
                       └───────────────┬──────────────────────────┘
                                       │ expect/actual
                 ┌─────────────────────┴────────────────────────┐
                 ▼                                              ▼
      androidMain: AndroidCrashReporter            iosMain: IosCrashReporter
      └─▶ FirebaseCrashlytics.getInstance()        └─▶ NSNotificationCenter.postNotificationName(…)
                                                              │
                                                              ▼
                                             CrashlyticsKMPSwift (Swift Package)
                                             CrashlyticsCallbackHelper.shared
                                             └─▶ Crashlytics.crashlytics() (FirebaseCrashlytics SPM)
```

- **Android**: llama directamente al SDK de Firebase Crashlytics.
- **iOS**: Kotlin/Native no puede llamar a Firebase directamente, así que `IosCrashReporter` postea
  notificaciones vía `NSNotificationCenter`. El Swift Package `CrashlyticsKMPSwift` las escucha
  (`CrashlyticsCallbackHelper`) y ejecuta las llamadas reales sobre `Crashlytics.crashlytics()`
  (`CrashlyticsBridge`). Es fire-and-forget: no hay notificaciones de vuelta Swift → Kotlin.

## 📦 Instalación

### Gradle (Android + commonMain), vía JitPack

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

```kotlin
// build.gradle.kts del módulo consumidor (p. ej. composeApp)
dependencies {
    implementation("com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp:1.0.0")
}
```

> ⚠️ El **módulo de aplicación Android** debe aplicar los plugins `com.google.gms.google-services` y
> `com.google.firebase.crashlytics` (y tener su `google-services.json`) — `CrashlyticsKMP` trae el
> SDK de Firebase Crashlytics como dependencia `api`, pero no sustituye la configuración del
> proyecto Firebase de la app consumidora.
>
> ⚠️ Si el módulo de aplicación usa minificación (R8/ProGuard), debe añadir
> `-renamesourcefileattribute SourceFile` en su **propio** fichero de reglas. La librería aporta
> `-keepattributes SourceFile,LineNumberTable` vía `consumer-rules.pro` (preserva fichero y línea),
> pero `-renamesourcefileattribute` es una opción **global** de R8 y AGP no permite que un AAR la
> declare en un `consumerProguardFile` — solo la app consumidora puede hacerlo.

Durante el desarrollo local (antes de publicar en JitPack), el módulo se consume como **composite
build**:

```kotlin
// settings.gradle.kts (raíz de la app)
includeBuild("CrashlyticsKMP") {
    dependencySubstitution {
        substitute(module("com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp"))
            .using(project(":crashlytics-kmp"))
    }
}
```

### Swift Package (iOS)

Ver [`SETUP_GUIDE.md`](SETUP_GUIDE.md) para los pasos completos en Xcode (Swift Package local/remoto,
`AppDelegate`, Run Script de subida de dSYM). Resumen:

```swift
import CrashlyticsKMPSwift

FirebaseApp.configure()
_ = CrashlyticsCallbackHelper.shared   // DESPUÉS de configure()
```

## 💻 Uso

### Android — `Application.onCreate()`

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        // "1.0.0" es un placeholder: usa el mecanismo de versión de tu propia app
        // (BuildConfig.VERSION_NAME, PackageInfo.versionName, etc.) — no lo provee esta librería.
        CrashlyticsKMP.initialize(
            CrashlyticsConfig(
                isDebugBuild = isDebug,
                defaultCustomKeys = mapOf(CrashlyticsKeys.APP_VERSION to "1.0.0")
            )
        )

        initKoin { /* … */ }
    }
}
```

### iOS — `configure` de `ComposeUIViewController`

```kotlin
@OptIn(ExperimentalNativeApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        // "1.0.0" es un placeholder: usa el mecanismo de versión de tu propia app
        // (NSBundle.mainBundle, etc.) — no lo provee esta librería.
        CrashlyticsKMP.initialize(
            CrashlyticsConfig(
                isDebugBuild = Platform.isDebugBinary,
                defaultCustomKeys = mapOf(CrashlyticsKeys.APP_VERSION to "1.0.0")
            )
        )
        initKoin()
    }
) { App() }
```

> En ambas plataformas, `initialize()` debe llamarse **antes** de `initKoin()` si se registra
> `single<CrashReporter> { CrashlyticsKMP.reporter }` en Koin: Koin cachea la instancia en la
> primera resolución, así que si `initKoin()` fuera primero, quedaría cacheado el
> `NoOpCrashReporter` previo a `initialize()` para siempre.

### API de `CrashReporter`

| Método | Descripción |
|---|---|
| `log(message: String)` | Log asociado al próximo reporte de crash. |
| `setUserId(userId: String?)` | Asocia el reporte a un usuario. `null` limpia el id. |
| `setCustomKey(key, value)` | 5 sobrecargas: `String`, `Boolean`, `Int`, `Long`, `Double`. |
| `setCustomKeys(keys: Map<String, Any>)` | Varias custom keys a la vez. |
| `recordException(throwable, message?, keys)` | Reporta como no fatal. |
| `setCollectionEnabled(enabled: Boolean)` | Activa/desactiva la recolección en runtime. |
| `sendUnsentReports()` | Fuerza el envío de reportes pendientes. |
| `deleteUnsentReports()` | Descarta los reportes pendientes. |
| `forceCrash()` | Provoca un crash real. Solo para verificación manual. |

`CrashlyticsKMP.reporter` nunca es `null` y ningún método de `CrashReporter` lanza: antes de
`initialize()`, y si `initialize()` falla, sirve `NoOpCrashReporter`.

## 📡 Protocolo de notificaciones Kotlin → Swift (iOS)

Todos los valores viajan como `String` (o `Map<String, String>` / `List<String>`), para evitar
problemas de boxing `NSNumber` desde Kotlin/Native. El lado Swift reconstruye el tipo cuando aplica.

| Notificación | `userInfo` |
|---|---|
| `CrashlyticsKMPLog` | `message: String` |
| `CrashlyticsKMPSetUserId` | `userId: String` (`""` ⇒ limpiar) |
| `CrashlyticsKMPSetCustomKey` | `key: String`, `value: String`, `type: "string"\|"bool"\|"int"\|"long"\|"double"` |
| `CrashlyticsKMPSetCustomKeys` | `keys: Map<String, String>` |
| `CrashlyticsKMPRecordError` | `name: String`, `reason: String`, `stackTrace: List<String>`, `keys: Map<String, String>` |
| `CrashlyticsKMPSetCollectionEnabled` | `enabled: String` (`"true"`/`"false"`) |
| `CrashlyticsKMPSendUnsentReports` | — |
| `CrashlyticsKMPDeleteUnsentReports` | — |
| `CrashlyticsKMPForceCrash` | — |

No hay notificaciones Swift → Kotlin en esta versión: la API es fire-and-forget.

## ⚠️ Limitaciones conocidas

- **El hook de excepciones Kotlin en iOS es best-effort, no una garantía.**
  `Crashlytics.record(exceptionModel:)` persiste de forma asíncrona, y el proceso termina justo
  después de postear la notificación — el reporte non-fatal puede no llegar a escribirse a tiempo.
  Cuando sí llega, Crashlytics además captura la señal del crash como *fatal* (sin stack Kotlin
  legible), y aparecen dos entradas correlacionadas en la consola; cuando no llega, solo queda la
  entrada fatal sin stack Kotlin. Unificarlas de forma fiable requeriría lanzar una `NSException`
  con los frames Kotlin (enfoque tipo CrashKiOS) — se deja para una v2.
- **Las custom keys de `recordException`/`recordError` son globales en ambas plataformas**, no
  por-reporte: ni Crashlytics Android ni Crashlytics iOS lo soportan. Quedan aplicadas hasta el
  siguiente `setCustomKey`.
- **Sin dSYM subido no hay símbolos** en iOS. Es responsabilidad de la app consumidora (ver
  `SETUP_GUIDE.md`, pasos c y d): el Run Script de subida de dSYM del target y la subida manual del
  dSYM de `ComposeApp.framework`.
- Los targets iOS y el Swift Package no se pueden compilar en Windows. El código `iosMain` se
  limita a `platform.Foundation`/`kotlin.native` para no romper la configuración en un entorno sin
  macOS.

## 🔒 Política de datos

**Nunca** enviar información personal identificable (email, nombre real, teléfono, etc.) a
Crashlytics, ni como custom key ni como `userId`. Usar **solo** el `uid` de Firebase. Consulta la
política de privacidad publicada en `docs/privacy-policy.html` de la app consumidora.

## 📄 Licencia

Ver [`LICENSE.md`](LICENSE.md).
