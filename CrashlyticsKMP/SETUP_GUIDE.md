# SETUP_GUIDE — CrashlyticsKMP en iOS

Esta guía cubre los pasos manuales en Xcode que **no se pueden automatizar desde Kotlin ni desde
Gradle**. La parte Kotlin/Android ya está cableada en la app (`CrashlyticsKMP.initialize()` en
`ListaCompraApp.onCreate()` y en el `configure` de `MainViewController`); esta guía completa el lado
iOS. Sigue los pasos en orden — no hace falta preguntar nada, están pensados para aplicarse tal cual.

## Estado de partida (verificado)

- `FirebaseCrashlytics` ya está enlazado en el target `iosApp` (`project.pbxproj:21,79,579-582`).
- `FirebaseApp.configure()` ya se llama en `iosApp/iosApp/iOSApp.swift:12`.
- `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym` ya está activo en Debug y Release — **no lo toques**.
- La única `PBXShellScriptBuildPhase` del proyecto es la de `embedAndSignAppleFrameworkForXcode`
  (`project.pbxproj:252`). **No existe ninguna fase de subida de dSYM.** Ese es el hueco real que
  cierra esta guía (pasos c y d).

---

## a) Añadir el Swift Package local

1. Abre `iosApp.xcodeproj` en Xcode.
2. **File → Add Package Dependencies… → Add Local…**
3. Selecciona la carpeta `CrashlyticsKMP` (la raíz, donde está `Package.swift` — mismo patrón que
   `AdMobKMPSwift`, ya referenciado en el proyecto como `XCLocalSwiftPackageReference` con
   `relativePath = ../AdMobKMPSwift`).
4. Target: `iosApp`. Producto a añadir: **`CrashlyticsKMPSwift`**.

Al terminar, el proyecto tendrá un `XCLocalSwiftPackageReference` con
`relativePath = ../CrashlyticsKMP`, igual que ya existe para `AdMobKMPSwift`.

## b) `iosApp/iosApp/iOSApp.swift`

Dentro de `didFinishLaunchingWithOptions`, **justo después** de `FirebaseApp.configure()`:

```swift
import CrashlyticsKMPSwift
// … resto de imports …

func application(_ application: UIApplication,
                 didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
    FirebaseApp.configure()
    _ = CrashlyticsCallbackHelper.shared   // debe ir DESPUÉS de configure()

    // … resto de la configuración …
    return true
}
```

**Por qué el orden es crítico:** `NSNotificationCenter` no encola nada. Todo lo que Kotlin postee
antes de que `CrashlyticsCallbackHelper.shared` registre sus observers se pierde en silencio, sin
ningún aviso — incluidos el `setCollectionEnabled` inicial y las custom keys base
(`platform`, `build_type`, `app_version`) que aplica `CrashlyticsKMP.initialize()`. Si el orden se
invierte, Crashlytics queda con su configuración por defecto y parece que "no funciona" cuando en
realidad nunca recibió nada.

## c) Run Script de subida de dSYM (lo que hoy falta)

`Target iosApp → Build Phases → + → New Run Script Phase`.

Colócala **después** de "Embed Frameworks" y de la fase `embedAndSignAppleFrameworkForXcode`
(la que ya existe para `ComposeApp.framework`).

Script:

```bash
"${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
```

**Input Files:**

```
${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Resources/DWARF/${TARGET_NAME}
$(SRCROOT)/$(BUILT_PRODUCTS_DIR)/$(INFOPLIST_PATH)
```

Desmarca **"Based on dependency analysis"** (el script necesita ejecutarse siempre, no solo cuando
Xcode detecta cambios en los inputs declarados).

`DEBUG_INFORMATION_FORMAT = dwarf-with-dsym` ya está activo en Debug y Release — no hay que
tocarlo, solo falta este Run Script para que el dSYM generado llegue a Firebase.

## d) dSYM del framework de Kotlin (`ComposeApp.framework`)

El Run Script del paso (c) sube el dSYM del target `iosApp`, pero `ComposeApp.framework` (el
framework generado por Kotlin/Native) genera su **propio** dSYM por separado, fuera del build de
Xcode. Sin subirlo, los crashes que ocurren en código Kotlin no se simbolizan aunque el resto sí.

### Vía principal: Firebase Console

**Firebase Console → Crashlytics → Missing dSYMs.** Firebase lista los dSYMs que le faltan; arrastra
ahí el `.dSYM` de `ComposeApp.framework`. Es la vía recomendada porque no depende de rutas frágiles
de `DerivedData` que cambian entre máquinas y entre builds.

Para localizar el `.dSYM` en tu proyecto:

```bash
find composeApp/build -name "*.dSYM"
```

(lo genera el build de Kotlin/Native, normalmente en
`composeApp/build/bin/<target>/<buildType>Framework/ComposeApp.framework.dSYM`).

### Alternativa avanzada: terminal

`upload-symbols` **no** es el mismo script que el Run Script Phase del paso (c): aquel usa
`$BUILD_DIR`, una variable que solo existe dentro del entorno de build de Xcode — en una terminal
normal está vacía y el comando falla con "no such file or directory". Desde terminal hay que apuntar
a la ruta real dentro de `DerivedData`:

```bash
~/Library/Developer/Xcode/DerivedData/<proyecto>-<hash>/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/upload-symbols \
  -gsp "<ruta a GoogleService-Info.plist>" -p ios "<ruta al .dSYM de ComposeApp>"
```

- `<proyecto>-<hash>`: mira la carpeta exacta dentro de `~/Library/Developer/Xcode/DerivedData/`
  (Xcode crea una por proyecto, con un hash que cambia si se borra DerivedData).
- `<ruta al .dSYM de ComposeApp>`: la que devuelve el `find` de arriba.

> ⚠️ `GoogleService-Info.plist` está en `.gitignore` (líneas 42 y 75 del `.gitignore` raíz) y no
> está en el árbol del repo. **Confirma su ubicación real en tu Mac** (normalmente
> `iosApp/iosApp/GoogleService-Info.plist`) y ajusta la ruta de `-gsp` en consecuencia antes de
> ejecutar el comando — no la copies literal sin verificarla.

## e) Verificar `GoogleService-Info.plist`

Confirma que el archivo:
- Existe en tu máquina (está gitignored, así que no viene en el repo).
- Está añadido al target `iosApp` en Xcode (aparece en "Copy Bundle Resources").

Sin él, `FirebaseApp.configure()` falla silenciosamente y nada de esto funciona.

## f) Cómo probar de verdad

Tres trampas que hacen que "no llegue nada" aunque todo lo anterior esté bien:

1. **Crashlytics no captura crashes con el depurador de Xcode conectado.** Lanza la app desde el
   springboard del dispositivo/simulador (Product → Run, luego para el depurador o desconéctalo, o
   abre la app directamente desde el icono), no dejes Xcode "escuchando" el proceso.
2. **Hay que reabrir la app** después del crash para que el reporte pendiente se suba.
3. **En debug la recolección está desactivada por diseño.** `CrashlyticsKMP.initialize()` aplica
   `setCollectionEnabled(!isDebugBuild || collectionEnabledInDebug)`, y `collectionEnabledInDebug`
   por defecto es `false`. Para probar de verdad:
   - Usa un build **release**, o
   - Pasa `collectionEnabledInDebug = true` en el `CrashlyticsConfig` de forma temporal (solo para
     la prueba, no lo dejes así en el código que se commitea).

## g) Gradle — cómo entra el módulo (informativo, ya está hecho)

El módulo `crashlytics-kmp` entra al build de la app como **composite build**:

```kotlin
// settings.gradle.kts (raíz)
includeBuild("CrashlyticsKMP") {
    dependencySubstitution {
        substitute(module("com.github.BonyGoD.CrashlyticsKMP:crashlytics-kmp"))
            .using(project(":crashlytics-kmp"))
    }
}
```

Si el sync de Gradle falla y sospechas que es por `org.gradle.configuration-cache=true` (activo en
`CrashlyticsKMP/gradle.properties`), **no cambies de estrategia por tu cuenta**. Hay un Plan B
documentado en la FASE 6.1 de `CRASHLYTICS_KMP_PLAN.md`:

```kotlin
include(":crashlytics-kmp")
project(":crashlytics-kmp").projectDir = file("CrashlyticsKMP/crashlytics-kmp")
```

(en ese caso el módulo pasaría a usar el catálogo de versiones del root, y habría que añadir ahí
`firebase-crashlytics`). Es una decisión tuya, coméntala antes de aplicarla.
