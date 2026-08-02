# Plan de desarrollo — CrashlyticsKMP (librería KMP Android + iOS)

> **Destinatario:** Sonnet 5 (KMP senior developer).
> **Autor del plan:** análisis previo del repo `Lista-compra-app` (rama `develop`).
> **Modo de trabajo:** implementación por fases. Al terminar cada fase se para y se revisa antes de continuar.

---

## 0. Resumen ejecutivo

Crear una librería KMP propia, `CrashlyticsKMP`, en una carpeta autocontenida dentro del repo
(`C:\Users\bony1\projects\Lista-compra-app\CrashlyticsKMP`), que exponga una API Kotlin común de
crash reporting funcionando en **Android** (Firebase Crashlytics SDK vía BoM) e **iOS** (Swift Package
propio + FirebaseCrashlytics vía SPM, comunicándose con Kotlin/Native por `NSNotificationCenter`).

La carpeta debe poder extraerse tal cual a su propio repo GitHub y publicarse en JitPack, siguiendo
exactamente el patrón ya validado en `C:\Users\bony1\projects\SignInKMP`.

Mientras tanto se integra en local mediante **composite build** (`includeBuild`) + **Swift Package local**,
para poder iterar sin publicar nada.

---

## 1. Estado actual (auditoría hecha sobre el repo)

### Android — Crashlytics YA funciona
| Elemento | Ubicación | Estado |
|---|---|---|
| Plugin `com.google.firebase.crashlytics` | `androidApp/build.gradle.kts:7` | ✅ aplicado |
| Plugin declarado en root | `build.gradle.kts` | ✅ `apply false` |
| Versión plugin `3.0.7` / BoM `34.16.0` | `gradle/libs.versions.toml` | ✅ |
| `mappingFileUploadEnabled = false` en debug | `androidApp/build.gradle.kts:71-75` | ✅ |
| `ndk { debugSymbolLevel = "FULL" }` en release | `androidApp/build.gradle.kts:94-96` | ✅ |
| `google-services.json` | `androidApp/google-services.json` | ✅ (gitignored) |
| Configuración runtime | `composeApp/src/androidMain/.../MainActivity.kt:38-47` | ⚠️ `configureCrashlytics()` privado, acoplado a la Activity, con `setCustomKey("build_type", …)` y `isCrashlyticsCollectionEnabled = !isDebug` |

### iOS — Crashlytics está enlazado pero NO configurado ni usable
| Elemento | Estado |
|---|---|
| Producto `FirebaseCrashlytics` linkado en el target | ✅ (`iosApp.xcodeproj/project.pbxproj:21,79,579-582`) |
| `FirebaseApp.configure()` en `AppDelegate` | ✅ (`iosApp/iosApp/iOSApp.swift:12`) |
| Firebase iOS SDK por SPM, `12.7.0+` | ✅ |
| **Run Script de subida de dSYM** | ❌ **NO EXISTE** — la única `PBXShellScriptBuildPhase` del proyecto es la de `embedAndSignAppleFrameworkForXcode` (`project.pbxproj:252`). Sin esto los crashes llegan **sin simbolizar**. |
| `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym` | ✅ ya está en Debug y Release |
| API de Crashlytics accesible desde Kotlin | ❌ no existe nada |
| `GoogleService-Info.plist` | ⚠️ gitignored (`.gitignore:75`), no está en el árbol; se asume que existe en la máquina del dev porque `FirebaseApp.configure()` funciona |
| Deployment target | iOS 15.6 |

### Kotlin común
- `dev.gitlive:firebase-crashlytics` **está declarado** (`libs.versions.toml` + `composeApp/build.gradle.kts`) pero **no se usa en ninguna línea de código Kotlin**. Dependencia muerta.
- Existe `core/analytics/AnalyticsService.kt` (wrapper sobre GitLive Analytics) — es el precedente estilístico a imitar para el wrapper de crash reporting.
- Errores: `core/CustomFailures/{LoginFailure, HomeFailures, FirebaseExceptionMapper}.kt`. Los datasources hacen `catch (e: Exception) { throw Exception("…", e) }` — puntos naturales para `recordException`.
- DI con Koin: `core/di/NetworkModule.kt` (`appModule`, `viewModelsModule`, `dataModule`, `initKoin`).
- Arranque: Android `ListaCompraApp.onCreate()`; iOS `MainViewController()` con `configure = { initKoin() }`.

### Patrón de puente Kotlin ↔ Swift ya usado en este proyecto
`AdMobKMPSwift/` (local, SPM) y `SignInKMPSwift` (remoto, dentro del repo `SignInKMP`) usan **`NSNotificationCenter`**:
Kotlin postea una notificación `XxxRequested` con `userInfo`, un singleton Swift (`AdMobCallbackHelper.shared`,
`SignInCallbackHelper.shared`) la escucha, ejecuta el SDK nativo y devuelve el resultado con otra notificación.
Ese singleton se inicializa en `AppDelegate` (`_ = AdMobCallbackHelper.shared`).

### Referencia de estructura publicable: `C:\Users\bony1\projects\SignInKMP`
```
SignInKMP/
├── Package.swift              # manifiesto SPM en la RAÍZ, path → SignInKMPSwift/Sources/SignInKMPSwift
├── jitpack.yml                # jdk openjdk17 + ./gradlew publishToMavenLocal
├── settings.gradle.kts        # include(":signin-kmp")
├── build.gradle.kts           # plugins apply false
├── gradle/libs.versions.toml  # catálogo propio
├── gradle.properties
├── signin-kmp/                # módulo Gradle KMP publicado (maven-publish, group com.github.BonyGoD, version 2.0.1)
├── SignInKMPSwift/Sources/…   # código Swift
├── iosApp/                    # app demo
├── README.md, SETUP_GUIDE.md, LICENSE.md
```
Consumido como `com.github.BonyGoD.SignInKMP:signin-kmp:2.0.1` (JitPack) + `XCRemoteSwiftPackageReference "SignInKMP"`.

---

## 2. Decisiones ya tomadas (no reabrir)

1. **Motor iOS: bridge Swift propio.** Swift Package `CrashlyticsKMPSwift` + `NSNotificationCenter`.
   No se usa GitLive. La dependencia `gitlive-firebase-crashlytics` queda marcada para eliminación en la fase final.
2. **Sonnet NO edita `iosApp/iosApp.xcodeproj/project.pbxproj`.** Se entrega `SETUP_GUIDE.md` con los pasos
   exactos para hacerlo desde Xcode. El riesgo de corromper el pbxproj no compensa.
3. **Alcance de integración en la app:** cableado completo (Android + iOS + Koin) **+ puntos clave**
   (`recordException` en los `catch` de datasources/repositories, `setUserId` al login, custom keys base,
   acción de test crash). Sin refactor masivo del manejo de errores.
4. Nombres fijados:
   - Carpeta / futuro repo: `CrashlyticsKMP`
   - Módulo Gradle: `crashlytics-kmp`
   - Paquete Kotlin: `dev.bonygod.crashlytics.kmp`
   - Swift Package / producto: `CrashlyticsKMPSwift`
   - Coordenadas JitPack: `com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp:1.0.0`
   - Prefijo de notificaciones: `CrashlyticsKMP…`

---

## 3. Arquitectura objetivo

```
                       ┌──────────────────────────────────────────┐
  App (commonMain) ──▶ │  CrashlyticsKMP.initialize(config)        │
                       │  CrashlyticsKMP.reporter : CrashReporter  │
                       └───────────────┬──────────────────────────┘
                                       │ expect/actual
                 ┌─────────────────────┴────────────────────────┐
                 ▼                                              ▼
      androidMain: AndroidCrashReporter            iosMain: IosCrashReporter
      └─▶ FirebaseCrashlytics.getInstance()        └─▶ NSNotificationCenter.post("CrashlyticsKMP…")
                                                              │
                                                              ▼
                                             CrashlyticsKMPSwift (Swift Package)
                                             CrashlyticsCallbackHelper.shared
                                             └─▶ Crashlytics.crashlytics() (FirebaseCrashlytics SPM)
```

### Árbol de la carpeta a crear

```
Lista-compra-app/
└── CrashlyticsKMP/                        ← NUEVA carpeta, autocontenida y extraíble
    ├── Package.swift                      ← manifiesto SPM (raíz, igual que SignInKMP)
    ├── settings.gradle.kts
    ├── build.gradle.kts
    ├── gradle.properties
    ├── gradle/
    │   ├── libs.versions.toml
    │   └── wrapper/{gradle-wrapper.jar, gradle-wrapper.properties}   ← copiar del root (Gradle 9.5.0)
    ├── gradlew, gradlew.bat               ← copiar del root
    ├── jitpack.yml
    ├── .gitignore
    ├── LICENSE.md
    ├── README.md
    ├── SETUP_GUIDE.md                     ← pasos Xcode + Gradle para consumidores
    ├── crashlytics-kmp/                   ← módulo Gradle KMP
    │   ├── build.gradle.kts
    │   └── src/
    │       ├── commonMain/kotlin/dev/bonygod/crashlytics/kmp/
    │       │   ├── CrashReporter.kt
    │       │   ├── CrashlyticsConfig.kt
    │       │   ├── CrashlyticsKMP.kt
    │       │   ├── CrashlyticsKeys.kt
    │       │   ├── NoOpCrashReporter.kt
    │       │   └── internal/PlatformCrashReporter.kt      (expect)
    │       ├── androidMain/
    │       │   ├── AndroidManifest.xml
    │       │   └── kotlin/dev/bonygod/crashlytics/kmp/internal/
    │       │       ├── PlatformCrashReporter.android.kt   (actual)
    │       │       └── AndroidCrashReporter.kt
    │       ├── iosMain/kotlin/dev/bonygod/crashlytics/kmp/internal/
    │       │   ├── PlatformCrashReporter.ios.kt           (actual)
    │       │   ├── IosCrashReporter.kt
    │       │   ├── CrashlyticsNotifications.kt
    │       │   └── KotlinExceptionHook.kt
    │       └── commonTest/kotlin/dev/bonygod/crashlytics/kmp/
    │           └── CrashlyticsKMPTest.kt
    └── CrashlyticsKMPSwift/
        ├── .gitignore
        ├── README.md
        └── Sources/CrashlyticsKMPSwift/
            ├── CrashlyticsCallbackHelper.swift
            ├── CrashlyticsBridge.swift
            └── KotlinStackTraceMapper.swift
```

---

## 4. Contrato público (fijar esto antes de escribir implementaciones)

### 4.1 API Kotlin (commonMain)

```kotlin
package dev.bonygod.crashlytics.kmp

interface CrashReporter {
    fun log(message: String)

    fun setUserId(userId: String?)                      // null ⇒ limpia el id

    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
    fun setCustomKey(key: String, value: Int)
    fun setCustomKey(key: String, value: Long)
    fun setCustomKey(key: String, value: Double)
    fun setCustomKeys(keys: Map<String, Any>)

    /** No fatal. [keys] se aplican solo a este reporte cuando la plataforma lo permite. */
    fun recordException(
        throwable: Throwable,
        message: String? = null,
        keys: Map<String, Any> = emptyMap()
    )

    fun setCollectionEnabled(enabled: Boolean)
    fun sendUnsentReports()
    fun deleteUnsentReports()

    /** Solo para verificación manual. Nunca en un flujo de usuario. */
    fun forceCrash()
}
```

```kotlin
data class CrashlyticsConfig(
    val isDebugBuild: Boolean,
    val collectionEnabledInDebug: Boolean = false,
    /** iOS: instala setUnhandledExceptionHook para reportar excepciones Kotlin no capturadas. */
    val installKotlinExceptionHook: Boolean = true,
    val defaultCustomKeys: Map<String, String> = emptyMap(),
    /** Escribe println() de diagnóstico del propio wrapper. */
    val verboseLogging: Boolean = false
)

object CrashlyticsKMP {
    val isInitialized: Boolean
    /** Antes de initialize() devuelve NoOpCrashReporter (nunca null, nunca lanza). */
    val reporter: CrashReporter
    fun initialize(config: CrashlyticsConfig)
}

object CrashlyticsKeys {
    const val PLATFORM = "platform"
    const val BUILD_TYPE = "build_type"
    const val APP_VERSION = "app_version"
    const val SCREEN = "screen"
    const val LAST_ACTION = "last_action"
}
```

Reglas de comportamiento no negociables:
- **Ninguna** llamada de la API puede lanzar. Todo va envuelto en `runCatching`/`try` como hace `AnalyticsService`.
- Llamar a cualquier método antes de `initialize()` es no-op silencioso (`NoOpCrashReporter`).
- `initialize()` es idempotente: segunda llamada = no-op + log si `verboseLogging`.
- En `initialize()` se aplican automáticamente: `PLATFORM` (`android`/`ios`), `BUILD_TYPE` (`debug`/`release`),
  `defaultCustomKeys`, y `setCollectionEnabled(!isDebugBuild || collectionEnabledInDebug)`.
- **Nunca** loguear PII (email, contraseña, nombre real). Solo el `uid` de Firebase. Documentarlo en el README.

### 4.2 Protocolo de notificaciones Kotlin → Swift (iOS)

Todos los valores de `userInfo` viajan como **String** (más un campo `type` cuando aplica), para evitar
problemas de boxing NSNumber desde Kotlin/Native. El lado Swift reconstruye el tipo.

| Notificación | `userInfo` |
|---|---|
| `CrashlyticsKMPLog` | `message: String` |
| `CrashlyticsKMPSetUserId` | `userId: String` (`""` ⇒ limpiar) |
| `CrashlyticsKMPSetCustomKey` | `key: String`, `value: String`, `type: "string"\|"bool"\|"int"\|"long"\|"double"` |
| `CrashlyticsKMPSetCustomKeys` | `keys: Map<String,String>` (todo string) |
| `CrashlyticsKMPRecordError` | `name: String`, `reason: String`, `stackTrace: List<String>`, `keys: Map<String,String>` |
| `CrashlyticsKMPSetCollectionEnabled` | `enabled: String` (`"true"`/`"false"`) |
| `CrashlyticsKMPSendUnsentReports` | — |
| `CrashlyticsKMPDeleteUnsentReports` | — |
| `CrashlyticsKMPForceCrash` | — |

No hay notificaciones Swift → Kotlin en la v1 (la API es fire-and-forget). Si en el futuro hace falta
`didCrashOnPreviousExecution()`, se añade el par request/response como en `AdPreloaderIsReadyRequested/Response`.

---

## 5. Fases

> Al final de cada fase: parar, resumir lo hecho y esperar visto bueno.
> **Nunca ejecutar tareas de build/compile/assemble/install** (regla de `CLAUDE.md`). La verificación la hace el usuario.

---

### FASE 1 — Esqueleto de la carpeta y build Gradle standalone

**Objetivo:** que `CrashlyticsKMP/` exista, sea autocontenida y esté lista para compilar como build independiente.

Archivos a crear:

1. `CrashlyticsKMP/settings.gradle.kts` — copiar el patrón de `SignInKMP/settings.gradle.kts`:
   `rootProject.name = "CrashlyticsKMP"`, bloques `pluginManagement`/`dependencyResolutionManagement`
   con `google { mavenContent { … } }` + `mavenCentral()` + `gradlePluginPortal()`, e `include(":crashlytics-kmp")`.
   **No** activar `TYPESAFE_PROJECT_ACCESSORS`.

2. `CrashlyticsKMP/gradle.properties` — ⚠️ **crítico**: un build incluido **no hereda** el `gradle.properties`
   del build raíz. Hay que replicar los flags que hacen funcionar AGP 9.2.1 + KMP en este entorno:
   ```properties
   kotlin.code.style=official
   kotlin.daemon.jvmargs=-Xmx4g
   kotlin.native.ignoreDisabledTargets=true
   kotlin.caching.enabled=true
   kotlin.incremental=true
   org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC -Dfile.encoding=UTF-8
   org.gradle.configuration-cache=true
   org.gradle.caching=true
   android.nonTransitiveRClass=true
   android.useAndroidX=true
   # Workaround AGP 9.0+: com.android.library incompatible con kotlin.multiplatform
   android.builtInKotlin=false
   android.newDsl=false
   ```

3. `CrashlyticsKMP/gradle/libs.versions.toml` — catálogo **propio** (no se comparte con el del root).
   Mínimo: `agp = "9.2.1"`, `kotlin = "2.4.10"`, `android-compileSdk = "37"`, `android-minSdk = "24"`,
   `firebase-bom = "34.16.0"`, plugins `androidLibrary` y `kotlinMultiplatform`, libs `firebase-bom`,
   `firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics" }`, `kotlin-test`.
   **Sin Compose, sin Koin** — la librería no debe arrastrar UI ni DI.

4. `CrashlyticsKMP/build.gradle.kts` — plugins `apply false` (patrón `SignInKMP/build.gradle.kts`).

5. Copiar del root: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`,
   `gradle/wrapper/gradle-wrapper.properties` (Gradle **9.5.0**).

6. `CrashlyticsKMP/crashlytics-kmp/build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.kotlinMultiplatform)
       alias(libs.plugins.androidLibrary)
       id("maven-publish")
   }

   kotlin {
       androidTarget {
           publishLibraryVariants("release")
           compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
       }
       val iosTargets = listOf(iosArm64(), iosSimulatorArm64(), iosX64())
       if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
           iosTargets.forEach { it.binaries.framework { baseName = "CrashlyticsKMP"; isStatic = true } }
       }
       sourceSets {
           commonMain.dependencies { /* nada */ }
           commonTest.dependencies { implementation(libs.kotlin.test) }
           androidMain.dependencies {
               api(project.dependencies.platform(libs.firebase.bom))
               api(libs.firebase.crashlytics)      // api, no implementation: el consumidor lo necesita
           }
       }
   }

   android {
       namespace = "dev.bonygod.crashlytics.kmp"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
       compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
       publishing { singleVariant("release") { withSourcesJar(); withJavadocJar() } }
   }

   group = "com.github.BonyGoD"
   version = "1.0.0"
   ```
   > El guard `os.name.contains("Mac")` para `binaries.framework` replica lo que hace
   > `composeApp/build.gradle.kts:27-37`; en Windows no se configura el framework nativo.
   > JVM 17 se elige para alinear con `composeApp`/`androidApp` (SignInKMP usa 11; aquí no aporta).

7. `crashlytics-kmp/src/androidMain/AndroidManifest.xml` mínimo (`<manifest/>` con el package implícito por namespace).

8. `CrashlyticsKMP/.gitignore` (basado en el de `SignInKMP`), `LICENSE.md` (MIT, copiar de `LICENSE.md` raíz),
   `jitpack.yml`:
   ```yaml
   jdk:
     - openjdk17
   install:
     - ./gradlew publishToMavenLocal
   ```

**Criterio de aceptación:** la carpeta contiene un build Gradle completo y autocontenido; ningún archivo del
proyecto raíz ha sido modificado todavía.

---

### FASE 2 — API común (commonMain)

Implementar `CrashReporter`, `CrashlyticsConfig`, `CrashlyticsKMP`, `CrashlyticsKeys`, `NoOpCrashReporter`
y el `expect internal fun createPlatformCrashReporter(config: CrashlyticsConfig): CrashReporter` tal como
está especificado en §4.1.

Detalles:
- `CrashlyticsKMP` guarda `@Volatile`-equivalente multiplataforma simple (`var` privado, la inicialización
  ocurre una sola vez en el arranque; no hace falta sincronización compleja, pero documentarlo).
- KDoc en español en toda la API pública (el resto del repo está documentado en español).
- `commonTest`: test de que `reporter` es `NoOpCrashReporter` antes de `initialize`, que `initialize` es
  idempotente y que ningún método lanza. (Solo test común, sin dependencias de plataforma.)

**Criterio de aceptación:** API cerrada y documentada; `commonMain` no importa nada de Firebase.

---

### FASE 3 — `actual` de Android

`AndroidCrashReporter` sobre `FirebaseCrashlytics.getInstance()`:
- `log` → `log()`; `setUserId` → `setUserId(userId ?: "")`; `setCustomKey` → sobrecargas nativas;
  `recordException` → `recordException(throwable)` precedido de `log(message)` y de los `keys` como
  `setCustomKey` (Crashlytics Android no tiene keys por-reporte; documentar que son globales hasta el siguiente set).
- `setCollectionEnabled` → `isCrashlyticsCollectionEnabled = enabled`.
- `sendUnsentReports()` / `deleteUnsentReports()` → equivalentes del SDK.
- `forceCrash()` → `throw RuntimeException("CrashlyticsKMP test crash (Android)")`.
- Todo dentro de `try/catch` con `printStackTrace()` en el catch (mismo estilo que `AnalyticsService`).

⚠️ **Requisito de orden:** `FirebaseCrashlytics.getInstance()` exige `FirebaseApp` inicializado. En la app eso
lo garantiza el `ContentProvider` del plugin `google-services`; aun así, documentar en el README que
`initialize()` debe llamarse desde `Application.onCreate()` o después.

**Criterio de aceptación:** paridad funcional con el `configureCrashlytics()` actual de `MainActivity`.

---

### FASE 4 — Swift Package `CrashlyticsKMPSwift`

1. `CrashlyticsKMP/Package.swift` (raíz de la carpeta, patrón `SignInKMP/Package.swift`):
   ```swift
   // swift-tools-version: 5.9
   import PackageDescription

   let package = Package(
       name: "CrashlyticsKMPSwift",
       platforms: [.iOS(.v15)],
       products: [.library(name: "CrashlyticsKMPSwift", targets: ["CrashlyticsKMPSwift"])],
       dependencies: [
           .package(url: "https://github.com/firebase/firebase-ios-sdk.git", from: "12.7.0")
       ],
       targets: [
           .target(
               name: "CrashlyticsKMPSwift",
               dependencies: [
                   .product(name: "FirebaseCore", package: "firebase-ios-sdk"),
                   .product(name: "FirebaseCrashlytics", package: "firebase-ios-sdk")
               ],
               path: "CrashlyticsKMPSwift/Sources/CrashlyticsKMPSwift"
           )
       ]
   )
   ```
   > `from: "12.7.0"` alinea con la referencia remota que ya usa `iosApp.xcodeproj`, evitando resolución doble.

2. `CrashlyticsBridge.swift` — `@objc public class` con métodos estáticos que envuelven
   `Crashlytics.crashlytics()`: `log`, `setUserId`, `setCustomKey(key:value:type:)`, `setCustomKeys`,
   `recordError(name:reason:stackTrace:keys:)`, `setCollectionEnabled`, `sendUnsentReports`,
   `deleteUnsentReports`, `forceCrash`.
   - `recordError` construye `ExceptionModel(name:reason:)`, asigna
     `model.stackTrace = frames.map { StackFrame(symbol: $0, file: "", line: 0) }` y llama
     `Crashlytics.crashlytics().record(exceptionModel: model)`.
   - `forceCrash()` → `fatalError("CrashlyticsKMP test crash (iOS)")`.
   - Logs con el mismo estilo del resto del proyecto: `print("🟣 [CrashlyticsKMP-Swift] …")`.

3. `KotlinStackTraceMapper.swift` — normaliza los frames que llegan de Kotlin
   (`kfun:dev.bonygod…#foo(){}` + `at …`) a símbolos legibles para el `ExceptionModel`.

4. `CrashlyticsCallbackHelper.swift` — singleton `@objc public class` con
   `@objc public static let shared`, `private init` que registra los observers de §4.2 y `deinit` que los
   quita (patrón exacto de `AdMobCallbackHelper.swift`).
   - **No** llamar a `FirebaseApp.configure()` desde aquí: lo hace la app.
   - Los handlers no necesitan hilo principal (Crashlytics es thread-safe), pero sí deben validar el
     `userInfo` con `guard let … else { print("❌ …"); return }`.

5. `CrashlyticsKMPSwift/README.md` — breve, patrón `AdMobKMPSwift/README.md`: instalación, uso en
   `AppDelegate`, tabla de notificaciones soportadas.

**Criterio de aceptación:** el paquete compila conceptualmente contra FirebaseCrashlytics 12.x y expone
un único punto de entrada (`CrashlyticsCallbackHelper.shared`).

---

### FASE 5 — `actual` de iOS + hook de excepciones Kotlin

1. `CrashlyticsNotifications.kt` — constantes con los nombres de §4.2 (fuente única de verdad, sin literales sueltos).

2. `IosCrashReporter.kt` — postea a `NSNotificationCenter.defaultCenter` con `@OptIn(ExperimentalForeignApi::class)`,
   convirtiendo todo a `String` y usando `mapOf<Any?, Any?>(…)` como en `AdComponents.ios.kt`.
   - `recordException` construye: `name = throwable::class.simpleName ?: "KotlinException"`,
     `reason = message ?: throwable.message ?: ""`,
     `stackTrace = throwable.getStackTrace().toList()` (`kotlin.native` expone `getStackTrace()` en `Throwable`),
     y añade la cadena de `cause` al final del stack como frames `"Caused by: …"`.

3. `KotlinExceptionHook.kt` — si `config.installKotlinExceptionHook`:
   ```kotlin
   @OptIn(ExperimentalNativeApi::class)
   internal fun installHook(reporter: CrashReporter) {
       setUnhandledExceptionHook { throwable ->
           reporter.recordException(throwable, message = "FATAL: unhandled Kotlin exception")
           terminateWithUnhandledException(throwable)
       }
   }
   ```
   ⚠️ **Limitación conocida a documentar en el README:** el reporte se registra como *non-fatal* justo antes
   de terminar el proceso; Crashlytics además capturará la señal del crash como fatal (sin stack Kotlin legible).
   El resultado práctico es que en la consola aparecen dos entradas correlacionadas. Es el comportamiento
   aceptado para la v1; una v2 puede lanzar una `NSException` con los frames Kotlin (enfoque tipo CrashKiOS)
   para unificarlas.

4. `PlatformCrashReporter.ios.kt` — `actual` que crea `IosCrashReporter`, aplica keys por defecto e instala el hook.
   Puede usar `kotlin.native.Platform.isDebugBinary` como *fallback* si el consumidor no especifica nada,
   pero `config.isDebugBuild` manda.

**Criterio de aceptación:** `iosMain` no referencia ningún símbolo Swift/ObjC de Firebase (solo `platform.Foundation`),
por lo que el módulo compila igual en Windows.

---

### FASE 6 — Integración en `Lista-compra-app`

Ahora sí se tocan archivos del proyecto raíz.

1. **`settings.gradle.kts` (raíz)** — añadir al final:
   ```kotlin
   includeBuild("CrashlyticsKMP") {
       dependencySubstitution {
           substitute(module("com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp"))
               .using(project(":crashlytics-kmp"))
       }
   }
   ```
   > La sustitución explícita es obligatoria: el `group` declarado en el módulo es `com.github.BonyGoD`,
   > mientras que JitPack publicará bajo `com.github.BonyGoD.CrashlyticsKMP`. Sin ella, Gradle no sustituiría.
   >
   > **Plan B** si el composite build da guerra con `org.gradle.configuration-cache=true`:
   > ```kotlin
   > include(":crashlytics-kmp")
   > project(":crashlytics-kmp").projectDir = file("CrashlyticsKMP/crashlytics-kmp")
   > ```
   > (en ese caso el módulo usa el catálogo del root y hay que añadir allí `firebase-crashlytics`).
   > Documentar cuál se ha usado; no cambiar de una a otra sin avisar.

2. **`gradle/libs.versions.toml` (raíz)** — añadir:
   ```toml
   crashlytics-kmp = "1.0.0"
   ...
   bonygod-crashlyticskmp = { module = "com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp", version.ref = "crashlytics-kmp" }
   ```
   y también `firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics" }` (para el BoM en `androidApp` si hiciera falta).

3. **`composeApp/build.gradle.kts`** — `commonMain.dependencies { implementation(libs.bonygod.crashlyticskmp) }`.
   No tocar nada más (la dependencia GitLive muerta se limpia en la Fase 8).

4. **Android — `ListaCompraApp.kt`**: inicializar **antes** de `initKoin`:
   ```kotlin
   val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
   CrashlyticsKMP.initialize(
       CrashlyticsConfig(
           isDebugBuild = isDebug,
           defaultCustomKeys = mapOf(CrashlyticsKeys.APP_VERSION to getPlatform().appVersion)
       )
   )
   ```
   `MainActivity.configureCrashlytics()` **se deja tal cual en esta fase** (aplica exactamente los mismos
   valores, así que no hay conflicto funcional). Su eliminación es la Fase 8.

5. **iOS — `MainViewController.kt`**:
   ```kotlin
   fun MainViewController() = ComposeUIViewController(
       configure = {
           CrashlyticsKMP.initialize(
               CrashlyticsConfig(
                   isDebugBuild = Platform.isDebugBinary,
                   defaultCustomKeys = mapOf(CrashlyticsKeys.APP_VERSION to getPlatform().appVersion)
               )
           )
           initKoin()
       }
   ) { App() }
   ```

6. **Koin — `core/di/NetworkModule.kt`**: `single<CrashReporter> { CrashlyticsKMP.reporter }` en `appModule`.

7. **Puntos clave de instrumentación** (inyección por constructor, actualizando los `get()` de `appModule`):
   - `login/data/datasource/UsersDataSource.kt` — en cada `catch (e: Exception)` que re-lanza
     (líneas ~144, 180, 253, 267): `crashReporter.recordException(e, "UsersDataSource.<método>")` **antes** del `throw`.
     Excluir el `catch` de la línea ~295 (fallback de nombre de lista, ruido esperado).
   - `home/data/datasource/ListaCompraDataSource.kt` — ídem en los `catch` que re-lanzan (~74, ~108).
   - `login/data/repository/UserRepository.kt` y `home/ui/ListaCompraViewModel.kt` — `recordException`
     en las ramas de error que hoy solo mapean a `LoginFailure`/`HomeFailures`. **No** reportar
     `LoginFailure` de validación (email mal escrito, contraseñas no coinciden): son errores de usuario, no bugs.
   - `login/ui/AuthViewModel.kt` — tras login/registro correcto (donde ya se navega con `usuario.uid`):
     `crashReporter.setUserId(uid)`; en `LogOutUseCase` → `setUserId(null)`.
     **Nunca** enviar email ni displayName.
   - `core/navigation/` — opcional: `setCustomKey(CrashlyticsKeys.SCREEN, ruta)` al navegar, si encaja
     sin ensuciar el `Navigator`. Si no encaja limpio, omitir y anotarlo.

8. **Acción de test crash** — algo accesible pero inofensivo: un `TextButton` oculto/condicionado a
   `isDebugBuild` en una pantalla ya existente (p. ej. perfil/ajustes) que llame a
   `CrashlyticsKMP.reporter.recordException(...)` y otro que llame a `forceCrash()`.
   Si no hay sitio natural, exponer solo las funciones y documentar cómo dispararlas manualmente.
   **No** dejar el botón visible en release.

**Criterio de aceptación:** la app compila (lo verifica el usuario), Android sigue reportando como antes,
iOS ya tiene la ruta Kotlin → Swift lista a falta de los pasos de Xcode.

---

### FASE 7 — Documentación y pasos manuales de Xcode

1. **`CrashlyticsKMP/SETUP_GUIDE.md`** (lo aplica el usuario en su Mac). Contenido obligatorio:

   **a) Añadir el Swift Package local**
   `Xcode → File → Add Package Dependencies… → Add Local… → seleccionar la carpeta CrashlyticsKMP`
   → target `iosApp` → producto `CrashlyticsKMPSwift`.
   (Queda como `XCLocalSwiftPackageReference relativePath = ../CrashlyticsKMP`, igual que `../AdMobKMPSwift`.)

   **b) `iosApp/iosApp/iOSApp.swift`** — dentro de `didFinishLaunchingWithOptions`, justo después de
   `FirebaseApp.configure()`:
   ```swift
   import CrashlyticsKMPSwift
   ...
   FirebaseApp.configure()
   _ = CrashlyticsCallbackHelper.shared   // debe ir DESPUÉS de configure()
   ```

   **c) Run Script de subida de dSYM (lo que hoy falta).**
   `Target iosApp → Build Phases → + → New Run Script Phase`, colocada **después** de `Embed Frameworks`
   y de la fase de `embedAndSignAppleFrameworkForXcode`:
   ```bash
   "${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
   ```
   *Input Files:*
   ```
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Resources/DWARF/${TARGET_NAME}
   $(SRCROOT)/$(BUILT_PRODUCTS_DIR)/$(INFOPLIST_PATH)
   ```
   Desmarcar *"Based on dependency analysis"*.
   Nota: `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym` ya está activo en Debug y Release, no hay que tocarlo.

   **d) dSYM del framework de Kotlin.** El `ComposeApp.framework` genera su propio dSYM; para que los
   crashes en código Kotlin/Native se simboliquen, subirlo con:
   ```bash
   "${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/upload-symbols" \
     -gsp "${SRCROOT}/iosApp/GoogleService-Info.plist" -p ios "<ruta al .dSYM de ComposeApp>"
   ```
   Documentar también la vía manual (Firebase Console → Crashlytics → *Missing dSYMs*).

   **e) Verificación de `GoogleService-Info.plist`** presente y añadido al target (está gitignored).

   **f) Cómo probar:** ejecutar en dispositivo/simulador **sin el depurador de Xcode adjunto**
   (Product → Run, luego parar el debugger, o lanzar la app desde el springboard), provocar el crash,
   y **reabrir la app** para que se suba el reporte.

2. **`CrashlyticsKMP/README.md`** — patrón `SignInKMP/README.md`: qué es, instalación Gradle (JitPack) + SPM,
   ejemplo de uso Android/iOS, tabla de la API, tabla de notificaciones, limitaciones conocidas
   (hook Kotlin, keys globales en Android, dSYM), política de "no PII", licencia MIT.

3. **`CRASHLYTICS_KMP_PLAN.md` (este archivo)** — actualizar al final con lo realmente implementado
   y las desviaciones respecto al plan.

4. `graphify update .` en la raíz del proyecto tras terminar los cambios de código (regla de `CLAUDE.md`).

**Criterio de aceptación:** el usuario puede seguir `SETUP_GUIDE.md` de principio a fin sin preguntar nada.

---

### FASE 8 — Limpieza (solo tras validar que funciona en local, requiere visto bueno explícito)

- Eliminar `configureCrashlytics()` de `MainActivity.kt` y sus imports (`FirebaseCrashlytics`).
- Eliminar `gitlive-firebase-crashlytics` de `composeApp/build.gradle.kts` y del catálogo (está sin usar).
- Revisar si `firebase-analytics` de GitLive y el resto siguen justificados (no tocar sin preguntar).

### FASE 9 — Extracción a repo propio (fuera de alcance hasta que funcione en local)

Checklist a dejar escrito, no ejecutar:
1. `git init` en una copia de `CrashlyticsKMP/`, repo `BonyGoD/CrashlyticsKMP`, tag `1.0.0`.
2. Build en JitPack → verificar `com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp:1.0.0`.
3. En `Lista-compra-app`: quitar el `includeBuild`, dejar solo la dependencia del catálogo.
4. En Xcode: sustituir el package local por `XCRemoteSwiftPackageReference` a
   `https://github.com/BonyGoD/CrashlyticsKMP` con versión exacta (patrón `SignInKMP`).
5. Borrar la carpeta `CrashlyticsKMP/` del repo de la app.

---

## 6. Verificación (la ejecuta el usuario, no Sonnet)

| # | Qué | Cómo | Esperado |
|---|---|---|---|
| 1 | Gradle sync | Android Studio | El módulo `crashlytics-kmp` aparece en el composite build |
| 2 | Android debug | `forceCrash()` en debug | **No** aparece en la consola de Firebase (colección desactivada) |
| 3 | Android release | APK release firmado, forzar crash, reabrir | Crash simbolizado + custom keys `platform`, `build_type`, `app_version` |
| 4 | Android non-fatal | Provocar error de red/Firestore | Entrada en *Non-fatals* con el mensaje del datasource |
| 5 | iOS | Tras `SETUP_GUIDE.md`, forzar crash sin debugger y reabrir | Crash en consola, simbolizado |
| 6 | iOS non-fatal | `recordException` desde Kotlin | Aparece con el stack Kotlin legible |
| 7 | userId | Login y crash posterior | El reporte lleva el `uid` de Firebase, sin email |

---

## 7. Riesgos y trampas conocidas

1. **`gradle.properties` no se hereda en composite builds** → sin `android.builtInKotlin=false` y
   `android.newDsl=false` el módulo con AGP 9.2.1 + KMP falla. Ya está previsto en Fase 1.
2. **Sustitución de dependencias**: el `group` del módulo (`com.github.BonyGoD`) no coincide con el de
   JitPack (`com.github.BonyGoD.CrashlyticsKMP`) → la sustitución explícita es obligatoria.
3. **`configuration-cache = true`** en el root puede chocar con el composite build; si ocurre, usar el Plan B
   de la Fase 6.1 antes de desactivar el cache global.
4. **Windows**: los targets iOS y el Swift Package no se pueden compilar aquí. El código `iosMain` debe
   limitarse a `platform.Foundation`/`kotlin.native` para no romper la configuración en Windows.
5. **Sin dSYM no hay símbolos.** Es el gap real de iOS hoy; el Run Script de la Fase 7 es tan importante
   como el código.
6. **Debugger adjunto**: Crashlytics no captura crashes con el depurador de Xcode conectado. Causa habitual
   de "no me llega nada".
7. **Firebase 12.x Swift API**: `Crashlytics.crashlytics()`, `ExceptionModel`, `StackFrame` (sin prefijo `FIR`).
   Verificar contra la versión resuelta antes de dar por buena la compilación.
8. **PII**: no enviar email/nombre a Crashlytics; hay política de privacidad publicada en `docs/privacy-policy.html`.
9. **Notificaciones desde hilos de fondo**: `NSNotificationCenter.post` entrega en el hilo emisor.
   Los handlers Swift no deben tocar UI (no lo hacen) — no meter `DispatchQueue.main.sync` (deadlock).

---

## 8. Reglas de trabajo para Sonnet

1. **Prohibido ejecutar** `build`, `assemble`, `compile`, `install`, `gradlew` de verificación (regla de `CLAUDE.md`).
   La compilación la hace el usuario.
2. **Prohibido tocar** `iosApp/iosApp.xcodeproj/project.pbxproj`.
3. Usar `graphify query/path/explain` para navegar el código antes de editar; `graphify update .` al terminar
   cada fase que toque código del proyecto raíz.
4. Idioma: código y KDoc en español, siguiendo el estilo existente (comentarios con emojis en logs,
   `try/catch` con `printStackTrace()` estilo `AnalyticsService`).
5. Una fase = un bloque de trabajo = un resumen al final. No encadenar fases sin visto bueno.
6. No introducir dependencias nuevas fuera de las listadas (nada de Compose, Koin, coroutines-extra ni
   librerías de logging en `crashlytics-kmp`).
7. Si algo del plan resulta técnicamente imposible o hay una alternativa claramente mejor: pararse,
   explicarlo y proponerla — no improvisar en silencio.

---

## 9. Estado real de la implementación

> Actualizado tras completar las Fases 1-7. Fases 8 y 9 siguen pendientes de ejecución.

### Fases completadas

- [x] **Fase 1** — Esqueleto Gradle standalone de `CrashlyticsKMP/`.
- [x] **Fase 2** — API común en `commonMain`.
- [x] **Fase 3** — `actual` de Android (`AndroidCrashReporter`).
- [x] **Fase 4** — Swift Package `CrashlyticsKMPSwift`.
- [x] **Fase 5** — `actual` de iOS (`IosCrashReporter`) + hook de excepciones Kotlin.
- [x] **Fase 6** — Integración en `Lista-compra-app`.
- [x] **Fase 7** — Documentación (`README.md`, `SETUP_GUIDE.md`, esta sección).

### Desviaciones respecto al plan original

1. **`consumer-rules.pro` (no estaba en el plan original, añadido en Fase 1).**
   R8 en `androidApp` corre con `isMinifyEnabled = true` y el `proguard-android-optimize.txt` de
   AGP 9.2.1 no preserva `SourceFile`/`LineNumberTable`. Sin esas reglas, los stack traces de
   Android en release llegarían a Firebase sin número de línea. Se añadió
   `crashlytics-kmp/consumer-rules.pro` con `-keepattributes SourceFile,LineNumberTable` y
   `consumerProguardFiles("consumer-rules.pro")` en el `defaultConfig` del módulo.
   >
   > **Matiz posterior:** la primera versión de `consumer-rules.pro` también incluía
   > `-renamesourcefileattribute SourceFile`, pero el build falló:
   > `':crashlytics-kmp:exportReleaseConsumerProguardFiles' > Global keep option
   > -renamesourcefileattribute was specified as a consumerProguardFile ... It should not be used
   > in a consumer configuration file.` `-renamesourcefileattribute` es una opción **global** de R8
   > y AGP prohíbe que viaje dentro de un `consumerProguardFile` (afectaría al build completo de
   > cualquier app consumidora); solo `-keepattributes` está permitido ahí. Se movió
   > `-renamesourcefileattribute SourceFile` a `androidApp/proguard-rules.pro` (la única app
   > consumidora hoy), y el README de la librería documenta que cualquier consumidor con
   > minificación debe declararla en su propio fichero de reglas.

2. **`explicitApi()` en el módulo `crashlytics-kmp`.** No estaba explícito en el plan; se activó en
   Fase 2 para que la superficie pública quedara cerrada y documentada desde el principio (todo
   `public` explícito, tipos de retorno explícitos), en línea con el criterio de aceptación de esa
   fase ("API cerrada y documentada").

3. **Paquetes `core/` e `internal/` en `commonMain`**, en vez de dejar todos los ficheros sueltos en
   la raíz de `dev.bonygod.crashlytics.kmp` como sugería el árbol original del §3. Se siguió el
   patrón de organización de `SignInKMP` (`dev.bonygod.signin.kmp.core/…`), pedido explícitamente
   durante la Fase 2.

4. **Stubs `actual` temporales en Fase 2.** Para que el módulo compilara al cerrar cada fase antes
   de tener la implementación real, `PlatformCrashReporter.android.kt` y
   `PlatformCrashReporter.ios.kt` devolvieron `NoOpCrashReporter` con un `// TODO(Fase N)` hasta que
   las Fases 3 y 5 los sustituyeron por `AndroidCrashReporter`/`IosCrashReporter` reales.

5. **`try/catch` por método (no por bloque) en `AndroidCrashReporter`.** Cada método de
   `CrashReporter` tiene su propio `try/catch(Throwable)` en vez de un único bloque envolviendo
   varias llamadas al SDK. En `recordException` y `setCustomKeys` esto es deliberado: un fallo en
   un paso (p. ej. una `key` suelta) no debe impedir que se ejecuten los pasos siguientes,
   especialmente el registro de la excepción en sí, que es lo único que `recordException` tiene
   que conseguir siempre.

6. **Correcciones C1-C3 en el Swift Package** (detectadas en revisión de Fase 4):
   - `setCrashlyticsCollectionEnabled(_:)` en vez de asignar la propiedad
     `isCrashlyticsCollectionEnabled` (de solo lectura en el SDK de iOS; en Android sí es correcta
     la asignación por propiedad).
   - `handleRecordError` ya no descarta el reporte entero si `stackTrace`/`keys` faltan o no
     castean: solo `name`/`reason` son obligatorios, el resto cae a `[]`/`[:]`.
   - `handleSetCollectionEnabled` distingue `"true"`/`"false"`/cualquier otro valor con un
     `switch` explícito, en vez de `enabledString == "true"` (que apagaba la recolección en
     silencio ante cualquier valor inesperado).

7. **Correcciones D1-D4 en la integración de Fase 6** (detectadas en revisión):
   - `MainViewController.kt` necesitó `@OptIn(ExperimentalNativeApi::class)` explícito para
     `Platform.isDebugBinary`, y `getPlatform().appVersion ?: "unknown"` (la interfaz común
     `Platform.appVersion` es `String?`, aunque `IOSPlatform` la sobrescriba como no-nulo).
   - **`UsersDataSource` finalmente NO quedó instrumentado con `recordException`.** El plan
     original (§Fase 6.7) pedía instrumentar sus catches de re-lanzamiento, pero
     `UserRepository` ya captura y reporta **todo** lo que este datasource lanza — instrumentar
     ambas capas habría duplicado (o triplicado, en el caso de `deleteAccount()` →
     `deletFirestoreAccount()`) el mismo reporte. Se revirtió el parámetro `crashReporter`, su
     import y las 4 llamadas a `recordException` en `UsersDataSource`; `UserRepository` queda como
     la única capa que reporta para el flujo de login/cuenta. Mismo criterio ya aplicado para no
     duplicar instrumentación en los ViewModels (ver punto 8).
   - Se documentó explícitamente en `NetworkModule.kt` que Koin cachea la instancia de
     `single<CrashReporter>` en la primera resolución, por lo que `CrashlyticsKMP.initialize()`
     debe ejecutarse antes de `initKoin()` en ambas plataformas.

8. **Instrumentación de `ListaCompraViewModel` parcial, por diseño.** Solo se instrumentaron los
   catches cuyo datasource subyacente no capturaba ya la excepción (`updateProducto`,
   `saveEditedProduct`, `borrarProducto`, `togglePurchased`). Se omitió deliberadamente en
   `addProducto()`/`borrarTodosLosProductos()` (ya reportados en `ListaCompraDataSource`) y en las
   ramas `onFailure` de `loadUserData()`/`acceptSharedList()`/`shareList()` (ya reportadas en
   `UserRepository`), para evitar duplicar el mismo reporte en dos capas.

9. **B8 (botón de test crash) omitido.** No existe hoy ningún flag de `isDebugBuild` accesible
   desde `commonMain`/la capa de UI — solo se calcula localmente en `ListaCompraApp.kt` (Android) y
   `MainViewController.kt` (iOS) para pasarlo a `CrashlyticsConfig`. Añadir uno nuevo solo para
   condicionar un botón habría sido un cambio de plumbing no pedido explícitamente. Disparo manual
   documentado: desde cualquier punto con `CrashReporter` inyectado (o `CrashlyticsKMP.reporter`
   directamente), llamar a `reporter.recordException(Exception("test"), "test")` o
   `reporter.forceCrash()`.

10. **Composite build sustituido por `project include` (Plan B de la FASE 6.1), tras completar la
    Fase 7.** El `includeBuild("CrashlyticsKMP") { dependencySubstitution { … } } }` no se estaba
    aplicando: al sincronizar, Gradle intentaba resolver
    `com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp:1.0.0` contra JitPack y fallaba, porque ese
    repo aún no existe (la publicación es la Fase 9). Se verificó que las coordenadas del catálogo
    raíz (`bonygod-crashlyticskmp`) y las de la regla `substitute(module(...))` coincidían carácter
    a carácter, así que no era un typo — es exactamente el escenario para el que el plan ya preveía
    el Plan B.
    - `settings.gradle.kts` (raíz): el `includeBuild` se sustituyó por
      `include(":crashlytics-kmp")` + `project(":crashlytics-kmp").projectDir = file("CrashlyticsKMP/crashlytics-kmp")`.
    - `composeApp/build.gradle.kts`: `implementation(libs.bonygod.crashlyticskmp)` →
      `implementation(project(":crashlytics-kmp"))`.
    - Al incluirse como proyecto del build de la app, el módulo dejó de usar su propio
      `CrashlyticsKMP/gradle/libs.versions.toml` y pasó a resolver `libs.*` contra el catálogo raíz.
      Solo faltaba una entrada: `firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics" }`
      (sin `version.ref`, fijada por el BoM — mismo patrón que `firebase-auth`), añadida junto a las
      demás entradas de Firebase. El resto de referencias (`kotlinMultiplatform`, `androidLibrary`,
      `android-compileSdk`, `android-minSdk`, `kotlin-test`, `firebase-bom`) ya existían.
    - `bonygod-crashlyticskmp` **no se borró** del catálogo: queda marcada con un comentario como
      sin uso mientras dure este montaje, para recuperarla en la Fase 9.
    - Los ficheros propios de `CrashlyticsKMP/` (su `settings.gradle.kts`, `gradle.properties`,
      catálogo, wrapper) no se tocaron: siguen ahí para que la carpeta sea extraíble tal cual en la
      Fase 9, simplemente se ignoran mientras el módulo se incluya así.
    - **La Fase 9 debe revertir este punto**: quitar el `include(":crashlytics-kmp")` y su
      `projectDir` de `settings.gradle.kts`, y volver a `implementation(libs.bonygod.crashlyticskmp)`
      en `composeApp/build.gradle.kts` (ver checklist actualizado abajo).

### Pendiente

- [ ] **Fase 8** — Limpieza: eliminar `configureCrashlytics()` de `MainActivity.kt` y la dependencia
  `gitlive-firebase-crashlytics`. Requiere visto bueno explícito tras validar en local (Android y,
  sobre todo, iOS con los pasos de `SETUP_GUIDE.md` aplicados en Xcode).
- [ ] **Fase 9** — Extracción a repo propio (`BonyGoD/CrashlyticsKMP`), publicación en JitPack y
  sustitución del composite build / Swift Package local por las referencias remotas. Incluye
  revertir el montaje temporal del punto 10 de arriba:
  - [ ] Quitar `include(":crashlytics-kmp")` y su `projectDir` de `settings.gradle.kts` (raíz).
  - [ ] Volver a `implementation(libs.bonygod.crashlyticskmp)` en `composeApp/build.gradle.kts`.
  - [ ] Reintroducir (o restaurar) el `includeBuild`/sustitución solo si se sigue queriendo iterar
    en local sin publicar; en otro caso, consumir directamente las coordenadas de JitPack.
