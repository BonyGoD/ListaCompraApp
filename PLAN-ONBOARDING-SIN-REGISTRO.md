# Plan — Onboarding sin registro (auth anónima)

**Rama:** `feature/onboarding-sin-registro` (creada desde `develop` el 17 ago 2026)

**Estado a 19 ago 2026:**

| Fase | Estado |
|---|---|
| 1 — Sesión anónima y arranque condicional | **Probada en Android y en iOS.** |
| 1 bis — Salidas hacia el login | **Probada en Android y en iOS.** |
| 2 — Desconectar el intersticial | **Probada en Android y en iOS.** |
| 3 — Vincular cuenta anónima | **Probada en Android y en iOS**, solo por correo. Google sigue bloqueado (ver abajo). |
| 3 bis — Arreglar listas compartidas | **Probada en Android y en iOS**, incluido rechazar y aceptar. |
| 4 — Idiomas | Auditada, cerrada y probada en los tres idiomas. |
| 5 — Arreglos de iOS (sección 23) | **Probados**: banner sin negro ni espera, botón en una línea, arranque blanco. |

**Los 44 pasos de `PRUEBAS-ONBOARDING.md` están en verde**, y el punto 3 del arreglo
de la sección 10 — el nombre de la lista compartida en `nombresListas` — quedó
implementado y **probado en iOS** el 19 ago 2026, al tercer intento. Falta repetirlo
en Android (bloque 10 bis) y, después, publicar.

**Pendiente y bloqueante para cerrar la rama:**
- **Habilitar el proveedor "Anonymous" en Firebase Console → Authentication →
  Sign-in method.** Viene desactivado por defecto y sin él `signInAnonymously()`
  devuelve `ADMIN_ONLY_OPERATION` y el Splash cae al estado de error. Detectado
  el 17 ago 2026 en la primera prueba sobre dispositivo: instalación limpia,
  petición de auth enviada, respuesta de error. **Este paso no estaba en el plan
  original**, que daba por hecho que `signInAnonymously()` funcionaba sin más.
- Las reglas de `notifications` (fase 1, punto 8) **hay que aplicarlas a mano en la
  consola de Firebase**: no existe `firestore.rules` en el repo.
- Compilar y probar en Android y en iOS.

**Diagnóstico de fallos del Splash:** el usuario solo ve el mensaje genérico de
`strings.xml`. El `e.message` real se guarda en `SplashState.errorMessage` pero no
se pinta en ningún sitio, así que **la fuente de verdad es Crashlytics**:
`UserRepository` ya hace `recordException` en `signInAnonymously`,
`getActualUser` y `repairUserDocument` antes de devolver el `Result.failure`.
Durante la depuración del 17 ago 2026 se usaron dos `println` temporales en
`SplashViewModel`; **retirados el mismo día**, una vez localizado el problema
(proveedor anónimo deshabilitado en la consola).

**Desviación consciente de la fase 2:** el plan decía cambiar `AuthViewModel` para
navegar a `Routes.Home`. Se hizo con un interruptor `AdConstants.INTERSTITIAL_ENABLED`
en `commonMain` y una salida temprana en `AdLoadingScreen`. El efecto observable es
el mismo —no se ve ningún intersticial— y conserva el flujo intacto para la futura
librería. `AuthViewModel` sigue navegando a `Routes.AdLoading`, que ahora rebota a
Home sin pintar anuncio.

---

## 1. Por qué

Datos reales de Play Console, 12 meses (ago 2025 – ago 2026):

| Métrica | Valor |
|---|---|
| Visitantes de la ficha | 68 |
| Instalaciones desde la ficha | 22 (**conversión 32%**, por encima de la media) |
| Usuarios nuevos | 35 |
| Usuarios perdidos | 25 |
| Retención a 7 días | 2 usuarios en 5 meses |
| Valoraciones | 1 |

La ficha convierte bien. **El problema es que lo que entra se va.** Y el primer
arranque explica por qué:

```
Navigator.kt:9        →  la pila arranca en Routes.Login, sin condición
AuthViewModel.kt:89   →  login OK      → Routes.AdLoading (intersticial)
AuthViewModel.kt:107  →  registro OK   → Routes.AdLoading (intersticial)
AuthViewModel.kt:148  →  Google OK     → Routes.AdLoading (intersticial)
AdLoadingScreen.kt:30 →  tras el anuncio → Routes.Home
```

Es decir: **muro de registro → anuncio a pantalla completa → app**. Incluso quien
acaba de registrarse se come el intersticial antes de ver una lista. Nadie crea
una cuenta para apuntar "leche".

## 2. Objetivo

Que alguien que acaba de instalar pueda **escribir su primer producto en menos de
cinco segundos, sin cuenta y sin anuncio**, y que la cuenta se pida solo cuando
aporta algo (compartir, o no perder una lista que ya le importa).

## 3. Decisión técnica: auth anónima, no Room

`signInAnonymously()` de Firebase devuelve un `uid` real. Todo el código actual
va indexado por `uid` (`usuarios/{uid}`, colección `lista-compra`, reglas de
seguridad, `getListas()`), así que **sigue funcionando sin cambios por debajo de
la capa de auth**. Room obligaría a escribir una persistencia nueva más un
sincronizador.

Cuando el usuario quiere compartir, `linkWithCredential()` convierte la cuenta
anónima en real **conservando el mismo `uid`**: cero migración, no pierde nada.

Contrapartida asumida: si el usuario borra los datos de la app o desinstala,
pierde la cuenta anónima. Afecta a los pocos que lo hacen a propósito; el muro
de registro afecta al 100% de quien instala.

## 4. Alcance

**Entra:**
- Sesión anónima automática al arrancar
- Arranque condicional de la navegación (fuera el `Routes.Login` hardcodeado)
- Quitar el intersticial del primer arranque
- Vincular cuenta anónima a Google / correo desde el punto de compartir
- Textos nuevos en los tres idiomas
- Cerrar el agujero de la regla de `notifications` (ver fase 1, punto 8)
- **Arreglar el bug de `addSharedList()` / `deleteOwnerList()`** (ver fase 3 bis
  y sección 10)

**No entra (no tocar en esta rama):**
- El bucle de invitación por correo a quien no tiene la app
- La In-App Review API, que ocupará el momento que libera el intersticial
- Cualquier cambio de ficha de Play

## 5. Reglas de arquitectura

Se respeta lo que ya hay, sin inventar patrones nuevos:

- **MVI por feature**: tríada `State` / `Event` / `Effect` en `ui/composables/interactions/`,
  `StateFlow` para estado, `SharedFlow` para efectos, `onEvent(...)` para eventos.
- **Capas**: `data/` (datasource, model, repository) → `domain/` (model, mapper,
  usecase) → `ui/` (screen, viewmodel, composables).
- **Repositorios devuelven `Result<T>`** con `try/catch` + `crashReporter.recordException(e, "Clase.metodo")`
  + `Result.failure(e.toUserFailure())`, exactamente como `UserRepository`.
- **Casos de uso finos**: `operator fun invoke` que delega en el repositorio.
- **Koin**: un único módulo en `core/di/NetworkModule.kt`. `single` para
  datasources y repositorios, `factory` para casos de uso, `viewModel` para
  ViewModels.
- **Tres idiomas obligatorios**: cualquier string va a `composeResources/values/`
  (inglés), `values-es/` y `values-ca/`. Ninguna cadena en duro.
- **Formato a mano**: 4 espacios, comas finales en listas multilínea, imports
  ordenados y sin comodines. **No se ejecuta Gradle ni Spotless.**

## 5 bis. Esto es KMP: todo tiene que funcionar en Android **y** iOS

- **`signInAnonymously()` y `linkWithCredential()`** son API de `dev.gitlive.firebase`
  y viven en `commonMain`: funcionan en las dos plataformas sin `expect/actual`.
  No hay que duplicar nada de la lógica de auth.
  > El subagente debe **verificar los nombres exactos de la API contra GitLive
  > 2.4.0** antes de escribir el código, no darlos por supuestos.
- **La persistencia de sesión la hace el SDK nativo, no GitLive.** El proyecto
  declara las dos capas: `dev.gitlive:firebase-auth:2.4.0` en `commonMain` y
  `com.google.firebase:firebase-auth` (BOM 34.16.0) en Android. GitLive es una
  envoltura fina sin estado propio, así que el comportamiento es el nativo:
  `SharedPreferences` en Android, Keychain en iOS.
  > **Como hoy no hay auto-login, la app nunca ha ejercitado esa persistencia.**
  > Comprobación rápida antes de empezar, con sesión iniciada:
  > `adb shell run-as dev.bonygod.listacompra ls shared_prefs`
  > Debe aparecer un `com.google.firebase.auth.api.Store.*.xml`.
  >
  > **Comprobada el 17 ago 2026 en Android: pasa.** Sobre build debug en un
  > Redmi 9 (`M2010J19SY`), con sesión iniciada, `shared_prefs` contiene:
  > ```
  > com.google.firebase.auth.api.Store.W0RFRkFVTFRd+...xml    7540 bytes
  > com.google.firebase.auth.api.crypto.W0RFRkFVTFRd+...xml
  > ```
  > No es un fichero vacío: hay token de refresco persistido, y el `.crypto.*`
  > es la clave con la que el SDK lo cifra. La persistencia nativa funciona; lo
  > único que falta es que alguien la consulte al arrancar, que es justo lo que
  > hace el Splash de la fase 1. Queda pendiente la prueba de actualización
  > sobre instalación existente (ver fase 1 bis) y la verificación en iOS.
  >
  > Nota de entorno: `ANDROID_HOME` no está configurada en la máquina de
  > desarrollo, así que `adb` hay que invocarlo por ruta completa desde
  > `%LOCALAPPDATA%\Android\Sdk\platform-tools`. Y `run-as` solo funciona sobre
  > build **debug**; en release firmada devuelve `package not debuggable`, que
  > no significa que el fichero no exista.
- **La creación de la credencial de Google sí es específica de plataforma.** Para
  la fase 3, reutilizar el mismo mecanismo que ya usa el login con Google
  actual, no montar uno nuevo.
- **Splash y diálogos** son Compose Multiplatform en `commonMain`: una sola
  implementación.
- **Desconectar el intersticial toca las dos plataformas**, aunque ya no se
  borre nada (ver fase 2). El cambio de navegación vive en `commonMain`
  (`AuthViewModel`), pero **la precarga se dispara por separado en cada
  plataforma** y hay que desactivar las dos:
  - `composeApp/src/androidMain/.../ListaCompraApp.kt:29` → `InterstitialAdManager.preloadAd(...)`
  - `iosApp/iosApp/iOSApp.swift:26-27` → `AdPreloader.shared.preloadAd(...)`

  Si solo se corta la de Android, iOS sigue pidiendo un anuncio en cada
  arranque que ya nunca se muestra.
- **Validación en las dos.** Los criterios de aceptación de la sección 8 se
  comprueban en Android y en iOS antes de dar la rama por buena. Recordatorio:
  el bloque `binaries.framework` de iOS está condicionado a macOS, así que la
  verificación de iOS solo puede hacerla el usuario desde Xcode.

## 6. Fases

Cada fase se valida antes de pasar a la siguiente.

### Fase 1 — Sesión anónima y arranque condicional

1. `login/data/datasource/UsersDataSource.kt`
   - `suspend fun signInAnonymously(): UserResponse` usando `auth.signInAnonymously()`
   - `fun isAnonymous(): Boolean` sobre `auth.currentUser?.isAnonymous`
   - Reutilizar la creación del documento `usuarios/{uid}` y de la lista por
     defecto que ya hace `userRegister()` (líneas ~137-143), extrayéndola a un
     privado compartido en lugar de duplicarla.
2. `login/data/repository/UserRepository.kt` — `suspend fun signInAnonymously(): Result<Usuario>`
   con el mismo patrón try/catch + crashReporter que el resto.
3. `login/domain/usecase/SignInAnonymouslyUseCase.kt` — nuevo, fino.
4. **Nueva ruta `Routes.Splash`** en `core/navigation/Routes.kt`, y
   `Navigator.kt:9` pasa a arrancar en `Routes.Splash` en lugar de `Routes.Login`.
5. `SplashViewModel` + `SplashScreen` (MVI, en `login/ui/`):
   - Si `getActualUser()` devuelve sesión → `Home(uid)`
   - Si no → `SignInAnonymouslyUseCase()` → `Home(uid)`
   - Si falla → `Routes.Login` como respaldo, con mensaje
6. `NavigationWrapper.kt` — registrar `entry<Routes.Splash>`.
7. `core/di/NetworkModule.kt` — registrar caso de uso y ViewModel nuevos.

> **Reglas de Firestore: comprobadas el 17 ago 2026, compatibles.**
> `usuarios/{userId}`, `lista-compra` y `productos` solo exigen
> `request.auth != null` más la propiedad por `uid`. Un usuario anónimo tiene
> `uid` real, así que no hay nada que cambiar para que la fase funcione.

8. **Cerrar el agujero de `notifications`** (mismo despliegue de reglas). Hoy es:
   ```
   match /notifications/{notificationId} {
     allow read, write, delete: if request.auth != null;
   }
   ```
   Cualquier usuario autenticado puede leer, escribir y borrar las
   notificaciones de todo el mundo, y contienen correos electrónicos. Con auth
   anónima, crear una cuenta pasa a ser gratis y de un toque, así que esta
   feature agrava el problema. Sustituir por:
   ```
   match /notifications/{notificationId} {
     allow create: if request.auth != null;
     allow read, delete: if request.auth != null
       && resource.data.email == request.auth.token.email;
   }
   ```
   Encaja con el diseño nuevo: para recibir una invitación hace falta cuenta
   real, así que `request.auth.token.email` existirá siempre en quien deba
   leerla.

### Fase 1 bis — Qué pasa con los usuarios que ya tienen la app

**Hallazgo del 17 ago 2026: hoy no existe auto-login.** `GetUserUseCase` solo lo
consumen `ListaCompraViewModel` y `MisListasViewModel`; `AuthViewModel` ni lo
inyecta. La app arranca siempre en `Routes.Login`, así que todos los usuarios
actuales teclean la contraseña en cada arranque en frío.

La sesión de Firebase **sí está persistida** en el dispositivo; simplemente nadie
la consulta al arrancar. El Splash de la fase 1 la consulta, y con eso:

| Usuario | Resultado |
|---|---|
| Con sesión activa | Entra directo a sus listas, **sin teclear la contraseña**. Mejora respecto a hoy. |
| Sin sesión (cerró sesión o reinstaló) | Sesión anónima → lista vacía. **Riesgo:** creerá que ha perdido sus datos. |

No hay migración de datos: no se toca ningún documento de Firestore y el `uid` de
los usuarios existentes no cambia.

**Publicar una actualización NO es una reinstalación.** Al actualizar desde Play,
el directorio de datos de la app se conserva, y con él la sesión de Firebase
Auth (`SharedPreferences` en Android, Keychain en iOS). La inmensa mayoría de los
usuarios actuales actualizarán y entrarán directos a sus listas. La sesión solo
se pierde con desinstalación o borrado manual de datos — y en iOS el Keychain
suele sobrevivir incluso a la desinstalación.

> **Prueba obligatoria antes de publicar:** instalar la versión actual, iniciar
> sesión, crear un producto, y **instalar la build nueva encima** (misma clave de
> firma, sin desinstalar). Tiene que abrir directamente en la lista con el
> producto ahí. Repetir en Android y en iOS. Si esto falla, no se publica.
>
> **Parcialmente comprobada el 17 ago 2026 en Android: la sesión sobrevive.**
> Con build debug sobre un Redmi 9, `adb install -r -t` del APK nuevo encima del
> instalado:
> ```
> firstInstallTime = 11:29:00   (no cambia: mismo directorio de datos)
> lastUpdateTime   = 11:44:11   (actualización real del paquete)
> auth.api.Store.*.xml   7540 bytes, 11:29   → intacto, mismo mtime
> auth.api.crypto.*.xml   537 bytes, 11:29   → intacto
> ```
> `admob.xml` y `measurement.prefs` sí se reescribieron a las 11:44, así que la
> app arrancó tras la actualización y no tocó la sesión.
>
> **Repetida el 17 ago 2026 con release firmada sobre release firmada: pasa.**
> APK firmado con `bonygod-upload.jks` (`CN=BonyGoD`, SHA-256
> `134f4a76…23f77a`), verificado con `apksigner` sobre el APK descargado del
> propio dispositivo — no es la clave de depuración, el fallback a debug signing
> de `build.gradle.kts:90` no se activó.
> ```
> firstInstallTime = 11:49:21   (sin cambios)
> lastUpdateTime   = 11:53:48   (actualización real)
> ceDataInode      = 139553     (EL MISMO directorio de datos)
> ```
> En release no se puede leer `shared_prefs` (`run-as` exige debuggable y el
> dispositivo no está rooteado), así que la medida es el `ceDataInode`: si no
> cambia, el directorio de datos es el mismo y el auth store va dentro. Que
> actualizar no reescribe ese fichero ya quedó probado en la pasada de debug.
>
> Nota: pasar de debug-firmada a release-firmada **sí** obliga a desinstalar
> (cambia la firma), y ahí se pierden los datos. Es un artefacto de la prueba,
> no del caso real: un usuario de Play siempre va release sobre release.
>
> **Completada el 17 ago 2026 en Android, ya con el Splash de la fase 1.**
> Procedimiento: compilar sin los cambios, iniciar sesión, cerrar la app,
> aplicar los cambios, recompilar. Resultado: **la app abre directamente dentro
> del último usuario logueado, sin pasar por el login.** El criterio observable
> queda verificado y con él el criterio de aceptación 6.
>
> **Sigue pendiente en iOS.**
>
> Trampa detectada al hacerlo: pulsar Run en Android Studio tras un cambio menor
> **no reinstala**, usa Apply Changes y `lastUpdateTime` no se mueve, con lo que
> la prueba da un falso positivo. Hay que forzar `adb install -r -t` sobre
> `androidApp/build/intermediates/apk/debug/` (no `outputs/`, que puede estar
> obsoleto) o subir `versionCode`. Verificar siempre `lastUpdateTime` antes de
> dar la prueba por hecha.

Para cubrir el segundo caso, **obligatorio en esta misma fase**:

1. **"Iniciar sesión" siempre accesible.** En `MenuLateral.kt`, cuando la sesión
   sea anónima, mostrar "Iniciar sesión / Crear cuenta" en lugar de "Cerrar
   sesión". Nunca puede quedar una pantalla sin salida hacia el login.
2. **Aviso en la lista vacía.** Si la sesión es anónima y la lista está vacía, el
   estado vacío incluye un "¿Ya tenías cuenta? Inicia sesión" que lleva a
   `Routes.Login`. Es justo donde mira el usuario desconcertado.
3. **Reutilizar el aviso de pérdida de datos.** Si un usuario anónimo **con
   productos ya escritos** inicia sesión en una cuenta existente, su lista
   anónima se descarta. Es el mismo caso que `credential-already-in-use` de la
   fase 3: debe salir el mismo diálogo de confirmación, no perder datos en
   silencio.

### Fase 2 — Desconectar el intersticial de la navegación (sin borrar código)

**Decidido el 17 ago 2026: el intersticial sale del flujo, pero el código se
conserva íntegro.** Rectifica la decisión anterior de eliminarlo: va a extraerse
a una librería KMP propia, al estilo de `SignInKMP` (que ya se consume como
`com.github.BonyGoD.SignInKMP:signin-kmp` vía JitPack), junto con el banner. Esa
extracción va en su propia rama; aquí solo se desconecta.

Datos de AdMob que justifican sacarlo del arranque:

| | Impresiones | Ingresos |
|---|---|---|
| Intersticial | 250 | **0,93 US$** |
| Banner | 576 | 0,22 US$ |

Noventa y tres céntimos por el anuncio peor colocado del ciclo de vida del
usuario. Que no rente ahí no significa que el código no valga: vale como
librería.

**Cambios (mínimos):**

1. `AuthViewModel.kt:89`, `:107` y `:148` → `Routes.Home(usuario.uid)` en lugar
   de `Routes.AdLoading(usuario.uid)`. Con esto ya no se llega nunca al
   intersticial: es el único cambio de comportamiento necesario.
2. **Desactivar también la precarga**, que hoy se dispara en cada arranque en
   frío y quedaría pidiendo anuncios que nadie va a ver:
   - `composeApp/src/androidMain/.../ListaCompraApp.kt:29` — `InterstitialAdManager.preloadAd(...)`
   - `iosApp/iosApp/iOSApp.swift:26-27` — `AdPreloader.shared.preloadAd(...)`

   Dejarla activa gastaría red y batería en el instante que esta rama quiere
   acelerar, y hundiría la tasa impresiones/peticiones de AdMob sin contrapartida.
   Se desactiva la **llamada**, no se borra el mecanismo.

**Se conserva tal cual, es material de la futura librería:**

```
composeApp/src/commonMain/.../ads/                    (AdConstants, ui/AdComponents, README)
composeApp/src/androidMain/.../ads/InterstitialAdManager.kt
composeApp/src/androidMain/.../ads/AdConstants.android.kt, ui/AdComponents.android.kt
composeApp/src/iosMain/.../ads/InterstitialAdPreloader.kt
composeApp/src/iosMain/.../ads/AdConstants.ios.kt, ui/AdComponents.ios.kt
composeApp/src/{common,android,ios}Main/.../login/ui/screens/ShowPreloadedInterstitial*.kt
composeApp/src/commonMain/.../login/ui/screens/AdLoadingScreen.kt
composeApp/src/commonMain/.../core/navigation/Routes.kt      → Routes.AdLoading se queda
composeApp/src/commonMain/.../core/navigation/NavigationWrapper.kt:36-39 → el entry se queda
AdMobKMPSwift/  e  iosApp/iosApp/AdPreloader.swift
iosApp/IOS_INTERSTITIAL_IMPLEMENTATION.md                    → ya no queda obsoleto
```

`Routes.AdLoading` y su `entry<>` quedan como código muerto alcanzable en una
línea: si algún día se quiere reactivar el intersticial en otro punto, es
cambiar una navegación. Cuesta seis líneas y ahorra reconstruir el cableado.
En release, R8 (`isMinifyEnabled = true`) ya elimina del APK lo que no se
referencia, así que conservar el fuente no engorda lo que se publica.

- **El banner se queda funcionando.** Se usa en `HomeContent.kt:163`, es pasivo
  y no bloquea nada.

> Se valoró recolocar el intersticial al borrar una lista completa. Se descarta:
> ese momento de satisfacción es donde debe ir la **In-App Review**, y los dos
> compiten por el mismo instante. Uno da céntimos; el otro da valoraciones, que
> es lo que gobierna el ranking y de las que hay exactamente una. Va en la rama
> siguiente.

> **Notas para cuando se monte la librería** (hallazgos del 17 ago 2026, no se
> tocan en esta rama):
> - `ads/ui/AdComponents.kt` expone `InterstitialAdTrigger`, una API genérica y
>   con precarga automática que **la app no usa**: el flujo real va por
>   `ShowPreloadedInterstitial` + `InterstitialAdManager`. Hay dos APIs de
>   intersticial conviviendo; la librería debería quedarse con una.
> - En iOS pasa lo mismo: `AdMobKMPSwift/Sources/.../AdMobInterstitialBridge.swift`
>   está empaquetado, pero quien hace el trabajo es `iosApp/iosApp/AdPreloader.swift`,
>   que vive en el target de la app. De `AdMobKMPSwift` solo se usa
>   `AdMobCallbackHelper.shared` (`iOSApp.swift:23`). Al extraer, `AdPreloader`
>   tiene que mudarse dentro del paquete.
> - `ads/README.md` ya tiene una sección "Migración a Librería" con el
>   procedimiento previsto.

### Fase 3 — Vincular cuenta anónima a cuenta real

1. `UsersDataSource` — `linkWithEmail(email, password)` y `linkWithGoogle(...)`
   sobre `auth.currentUser?.linkWithCredential(...)`.
2. Repositorio + casos de uso siguiendo el patrón.
3. **Punto de entrada en la UI**: al pulsar "Compartir lista" siendo anónimo, en
   vez del diálogo de compartir sale uno que explica que hace falta cuenta y
   lleva a crearla. Al volver, se abre el de compartir.
4. `MenuLateral.kt` — si es anónimo, mostrar "Crear cuenta" en lugar de "Cerrar
   sesión".
5. **Caso borde, decidido el 17 ago 2026**: ante `credential-already-in-use`
   (el correo ya tiene cuenta), avisar de forma explícita —"ese correo ya tiene
   una cuenta; si inicias sesión con ella, perderás la lista que has creado
   hasta ahora"— y dejar que el usuario confirme o cancele. Si confirma, se
   inicia sesión con la cuenta existente y se descarta la anónima.

> **Bloqueo encontrado el 18 ago 2026: no se puede vincular con Google.**
> `GoogleAuthHelper.processCredential()` de la librería `SignInKMP` hace
> `FirebaseAuth.signInWithCredential(...)` internamente y **no expone el `idToken`**
> (comprobado decompilando `signin-kmp-android-2.0.1.aar`; en iOS igual, el helper
> solo relaya un `uid` desde código nativo). Cuando la librería devuelve el control,
> `auth.currentUser` ya ha cambiado: la sesión anónima está **sustituida**, no
> vinculada, que es justo lo contrario de lo que persigue la fase.
>
> Opciones, por orden de preferencia:
> 1. **Modificar `SignInKMP`** para que exponga el `idToken` o tenga un modo de
>    vinculación. Lo limpio, pero toca otro repositorio y publicar en JitPack.
> 2. Duplicar el flujo de Credential Manager por plataforma dentro de esta app.
>    Contradice la regla de reutilizar; descartado salvo urgencia.
> 3. **Publicar solo con vinculación por correo.** Es como queda la rama.
>
> **Decidido: la 3 ahora, la 1 después en su propia rama.** Vincular por correo ya
> cubre el caso de uso, y arreglar la librería no debe retrasar esto.
>
> `LinkAccountDialog` **no tiene botón de Google**: un botón que sustituyera la
> sesión en silencio sería peor que no tenerlo.
>
> **Decidido el 18 ago 2026: el código de `linkWithGoogle` se borra.** Se había
> implementado en datasource, repositorio y caso de uso, registrado en Koin y sin
> ningún punto de entrada: un circuito cerrado que solo se referenciaba a sí mismo
> (comprobado con `grep`). Se elimina de los cuatro sitios porque, cuando
> `SignInKMP` exponga el `idToken`, la firma tendrá que cambiar para encajar con
> lo que la librería acabe ofreciendo — habría que reescribirlo igual. Guardarlo no
> ahorraba trabajo, solo dejaba algo que parecía funcionar y no funcionaba.
>
> **Comportamiento actual, a sabiendas:** un usuario anónimo que entre con Google
> desde el login **sustituye** su sesión y deja la lista anónima huérfana. No es lo
> deseable, pero no es silencioso: ese camino pasa antes por el diálogo de pérdida
> de datos de la fase 1 bis.

### Fase 3 bis — Arreglar el acumulado de listas compartidas

Va aquí, pegado a la fase 3, porque es la misma zona: la fase 3 empuja al usuario
anónimo hacia el botón de compartir, así que conviene que lo que hay al otro lado
esté sano antes de mandar gente. Diagnóstico completo en la sección 10.

Todo en `UsersDataSource.addSharedList()` (líneas 185-203):

1. **Acumular en vez de pisar.** Leer `listas`, añadir `listaId` si no está ya y
   escribir el resultado, con el mismo leer-modificar-escribir de
   `addNewLista()` (líneas 338-358). Sin duplicados si se acepta dos veces.
2. **Sacar de aquí la llamada a `deleteOwnerList()`** (línea 195). La función se
   queda tal cual: la usa `deleteAccount()` (línea 263), donde sí procede.
3. **Guardar el nombre de la lista compartida en `nombresListas`**, o `getListas()`
   caerá al fallback "Lista N" en cuanto las reglas no dejen leer el documento
   ajeno.
4. **Devolver el `UserResponse` con el array real**, no con `listOf(listaId)`.
   `acceptSharedList()` (`ListaCompraViewModel.kt:245`) pinta con lo que reciba.

5. **Normalizar el correo al compartir.** `ListaCompraViewModel.shareList()` pasa
   `email` **tal cual lo teclea el usuario**, sin `trim()` ni minúsculas, y así se
   guarda en el documento de `notifications`. Las reglas nuevas comparan ese campo
   con `request.auth.token.email`, así que `Bony@Gmail.com` y `bony@gmail.com`
   serían personas distintas. La regla ya normaliza por su cuenta (ver más abajo),
   pero el dato debe entrar limpio en origen en vez de parchearse al leerlo.

Criterio: un usuario con dos listas propias que acepta una invitación acaba con
**tres**, y las dos suyas conservan sus productos. Y una invitación enviada a
`Correo@Ejemplo.com` la recibe la cuenta `correo@ejemplo.com`.

### Fase 4 — Idiomas

Todas las cadenas nuevas en `values/`, `values-es/` y `values-ca/`.

## 7. Riesgos

- **Cuentas anónimas huérfanas** acumulándose en Firebase Auth. No cuestan
  dinero, pero conviene una limpieza periódica de las que no tienen datos.

  > **No confundir con la "limpieza automática" de Firebase Console.** Al
  > habilitar el proveedor anónimo, la consola ofrece una casilla que borra las
  > cuentas anónimas **con más de 30 días de antigüedad**. Decidido el 17 ago
  > 2026: **se deja desmarcada.** En esta app la cuenta anónima no es un residuo,
  > es la cuenta del usuario: todas sus listas cuelgan de ese `uid`. Activarla
  > garantizaría que quien lleve un mes usando la app sin registrarse pierda
  > todo, sin aviso y sin recuperación — exactamente el usuario que esta rama
  > quiere conservar. El único incentivo (que el uso anónimo no compute en las
  > cuotas de facturación) es irrelevante con 35 usuarios nuevos al año.
  >
  > La limpieza que sí interesa es selectiva: cuentas anónimas **sin datos
  > asociados**, no por antigüedad.
- **`DeleteAccountUseCase`** tiene que seguir funcionando con usuarios anónimos.
- **Sesión perdida** al borrar datos de la app: asumido, mitigado pidiendo la
  cuenta cuando el usuario ya tiene algo que perder.

## 8. Criterios de aceptación

Los valida el usuario compilando a mano:

1. Instalación limpia → la app abre directamente en la lista, sin login.
2. Se puede añadir un producto sin cuenta, y sigue ahí al cerrar y volver a abrir.
3. **Ningún intersticial en la primera sesión.**
4. Pulsar Compartir siendo anónimo ofrece crear cuenta; al crearla, la lista
   sigue estando (mismo `uid`).
5. El login con correo y con Google de siempre sigue funcionando.
6. **Un usuario con sesión activa previa entra directo a sus listas**, sin
   teclear la contraseña y sin perder nada.
7. **Desde una sesión anónima siempre se puede llegar al login**, tanto desde el
   menú lateral como desde el aviso de la lista vacía.
8. Los textos nuevos salen en los tres idiomas.
9. **Un usuario anónimo no ve ninguna notificación.** No tiene correo, así que no
   puede haber recibido invitaciones. (Criterio añadido el 18 ago 2026 tras verse
   lo contrario en dispositivo.)
10. **Aceptar una lista compartida ya no borra nada**: un usuario con dos listas
   propias que acepta una invitación acaba con tres, y las dos suyas conservan
   sus productos. Comprobar además en la consola de Firestore que no ha
   desaparecido ningún documento de `lista-compra`.
11. Todo lo anterior, comprobado **en Android y en iOS**.

## 9. Cómo trabajamos

El flujo de siempre:

- **Subagente Sonnet desarrolla**, fase a fase, sin saltarse ninguna.
- **Yo reviso el diff** de cada fase antes de dar paso a la siguiente.
- **El usuario compila y prueba.** Ni yo ni el subagente ejecutamos Gradle,
  Spotless ni ningún comando de compilación.
- Antes de tocar código, `graphify query` para localizar; grep solo para
  verificar detalles. Al terminar cada fase, `graphify update .`

---

## 10. Bug confirmado: aceptar una lista compartida borra el resto

**Reproducido por el usuario el 17 ago 2026.** Deja de ser una sospecha. Al
aceptar una invitación, las listas propias no se acumulan con la compartida: se
pierden.

### Qué pasa exactamente

`UsersDataSource.addSharedList()` (líneas 185-203) hace dos destrucciones, no una:

```kotlin
suspend fun addSharedList(listaId: String): UserResponse {
    val uid = auth.currentUser?.uid.orEmpty()
    firebase.collection("usuarios")
        .document(uid)
        .set(
            data = mapOf(
                "listas" to listOf(listaId)      // (1) pisa el array entero
            ),
            merge = true
        )
    deleteOwnerList()                            // (2) borra los documentos
    ...
}
```

1. **`merge = true` no salva el array.** El merge de Firestore es por campo, no
   por elemento: fusiona `listas` con lo que había a nivel de documento, pero el
   valor del campo se sustituye entero por `listOf(listaId)`. El usuario se queda
   con exactamente una entrada, la compartida. Todas sus referencias desaparecen.
2. **`deleteOwnerList()` (líneas 221-233) borra de verdad.** Recorre
   `lista-compra` con `where { "owner".equalTo(userUID) }` y hace
   `doc.reference.delete()` sobre **cada documento que el usuario posee**. No es
   quitar una referencia: son los documentos de las listas, borrados de Firestore.

El punto 1 se podría deshacer si se supiera qué IDs había. El punto 2 no: los
documentos ya no existen. Y como Firestore no borra en cascada, la subcolección
`productos` de cada lista queda huérfana, ocupando sitio y sin ruta que la
alcance.

### Por qué está así

Es un resto del modelo de **una lista por usuario**: aceptar una compartida
significaba "cambiar de lista", y por eso tenía sentido pisar el array y limpiar
la anterior. La feature de varias listas (`mislistas/`, `getListas()`,
`nombresListas`) llegó después y no revisó este camino.

Se ve en la comparación con el hermano correcto, `addNewLista()` (líneas 338-358),
escrito ya con el modelo nuevo:

```kotlin
val currentListas = (userDoc.get("listas") as? List<String>) ?: emptyList()
... "listas" to (currentListas + newListaId)     // lee, añade, escribe
```

`setDefaultLista()` y `renameNombreLista()` siguen el mismo patrón de
leer-modificar-escribir. `addSharedList()` es el único que no.

### Arreglo

1. **Acumular en vez de pisar**: leer `listas`, añadir `listaId` si no está ya, y
   escribir el resultado — igual que `addNewLista()`. Evitar duplicados si la
   invitación se acepta dos veces.
2. **Quitar la llamada a `deleteOwnerList()` de `addSharedList()`.** Aceptar una
   lista ajena no tiene por qué borrar las propias. La función se queda: la usa
   `deleteAccount()` (línea 263), donde sí procede.
3. **Guardar el nombre de la lista compartida en `nombresListas`.** Si no,
   `getListas()` intenta leer `lista-compra/{listaId}.nombre` y cae al fallback
   en cuanto las reglas no dejen leer el documento ajeno.
   > **HECHO el 19 ago 2026, y corregido el mismo día.** La primera versión leía el
   > nombre **antes** de escribir el array `listas`, y en iOS no guardaba nada en el
   > mapa. El motivo estaba ya documentado en `acceptSharedList()`: *"los permisos de
   > Firestore están propagados"* después de esa escritura. El usuario gana acceso al
   > documento de la lista ajena **porque** su id entra en `listas`, así que leerlo
   > antes falla, el `catch` devuelve `null` y no se escribe nada. En silencio,
   > además: el datasource no tiene `crashReporter`.
   >
   > Orden correcto, ya aplicado: escribir `listas` → leer el nombre → escribir
   > `nombresListas`. Son dos escrituras en vez de una, y la segunda se salta si el
   > id ya estaba en el mapa. El fallback real del código es `"Lista de la compra"`,
   > no `"Lista N"` como decía este plan.
   >
   > **Segundo intento fallido, y diagnóstico definitivo.** Reordenar no bastó: en
   > iOS seguía saliendo `"Lista de la compra"`. Y eso es la prueba de que no era un
   > problema de orden — si el fallback aparece, es que **`getListas()` tampoco puede
   > leer el documento de la lista ajena**. El destinatario no tiene permiso de
   > lectura sobre `lista-compra/{listaId}`, ni antes ni después de entrar en el
   > array. Cualquier solución que pase por leer esa lista desde quien la recibe
   > está condenada.
   >
   > **Solución aplicada: el nombre viaja con la invitación.**
   > `shareListaCompra()` lo resuelve del documento propio de quien comparte —
   > `usuarios/{uid}.nombresListas[listaId]`, con lectura de `lista-compra` como
   > respaldo, que ahí sí está permitida— y lo escribe como `listaNombre` en el
   > documento de `notifications`. De ahí baja por el DTO, el modelo de dominio, el
   > de UI y el evento, hasta `addSharedList(listaId, listaNombre)`, que vuelve a
   > escribir `listas` y `nombresListas` en una sola operación. **Sin lectura
   > cruzada y sin depender de las reglas.**
   >
   > Ficheros tocados: `UsersDataSource`, `UserRepository`, `AddSharedListUseCase`,
   > `ListaCompraViewModel`, `ListaCompraEvent`, `ShowNotificationsBottomSheet`,
   > `NotificationsReponse`, `Notifications`, `NotificationsUI` y los dos mappers.
   >
   > **Dos consecuencias que hay que tener presentes:**
   > - Las invitaciones **ya enviadas** no llevan `listaNombre`: llegan vacías, no se
   >   escribe nada en el mapa y la lista sigue mostrando el fallback. Para probar
   >   hay que compartir de nuevo.
   > - Las listas compartidas **ya aceptadas** tampoco tienen entrada en el mapa y
   >   seguirán con el fallback. Si molesta, hay que repararlas a mano en Firestore.
4. **Devolver el `UserResponse` con el array real**, no con `listOf(listaId)`.
   Hoy la respuesta miente aunque Firestore estuviera bien, y
   `acceptSharedList()` (`ListaCompraViewModel.kt:245`) la usa para pintar.

### Alcance del daño ya causado

Todo usuario que haya aceptado una invitación perdió sus listas propias en ese
momento. No hay copia de seguridad de los documentos borrados. Conviene mirar en
Firestore cuántos `usuarios` tienen un solo elemento en `listas` junto con
subcolecciones `productos` huérfanas, para saber a cuánta gente le pasó.

### Orden respecto a esta rama

**Decidido el 17 ago 2026: sale con el resto del plan, en esta misma rama.** Se
valoró sacarlo antes como corrección aparte, pero el bug lleva tiempo ahí y no
hay una avalancha de gente compartiendo listas: 35 usuarios nuevos en doce meses.
Adelantarlo obligaría a una publicación extra sin ganar casi nada.

Va en la **fase 3 bis**, justo después de la fase 3, por dos motivos:

- **La fase 3 lleva más gente aquí.** El punto de entrada de compartir es
  precisamente lo que empujará al usuario anónimo a crear cuenta. Conviene que el
  camino esté arreglado antes de mandar tráfico por él.
- **Comparte zona con la fase 1, punto 8** (reglas de `notifications`). Yendo en
  la misma rama, el despliegue de reglas se hace una vez y no hay que coordinar
  nada entre ramas.

---

## 11. Quitar los cuatro errores de `iosX64` en cada compilación

Cada build de Android escupe cuatro bloques rojos en
`:composeApp:checkKotlinGradlePluginConfigurationErrors`, uno por cada source set
(`appleMain`, `commonMain`, `iosMain`, `nativeMain`):

```
KMP Dependencies Resolution Failure
Couldn't resolve dependency 'org.jetbrains.compose.runtime:runtime:1.11.1' ...
The dependency should target platforms: [iosArm64, iosSimulatorArm64, iosX64]
Unresolved platforms: [iosX64]
```

Se repite para `compose.foundation`, `compose.ui`, `components-resources`,
`components-ui-tooling-preview`, `lifecycle-runtime-compose`,
`adaptive-navigation3` y `lifecycle-viewmodel-navigation3`.

### Causa

`composeApp/build.gradle.kts:23` declara el target **`iosX64()`** — el simulador
de iOS para Macs con procesador Intel. Las versiones que usa el proyecto ya no
publican artefactos para esa plataforma: Compose Multiplatform 1.11.1,
`org.jetbrains.androidx.lifecycle` 2.11.0 y `adaptive-navigation3` 1.3.0-beta02
solo traen `iosArm64` e `iosSimulatorArm64`. Gradle pide algo que no existe y
protesta cuatro veces, una por source set que hereda el target.

No rompen la compilación de Android — el build sigue y falla o termina más
adelante por otros motivos — pero ensucian todas las pasadas y esconden errores
de verdad.

### Arreglo

Borrar la línea `iosX64()` de `composeApp/build.gradle.kts:23`. **Es la única
referencia a `iosX64` en todo el repositorio** (comprobado el 17 ago 2026 con
`grep` sobre `.kts`, `.toml`, `.pbxproj` y ficheros de CI), así que no hay nada
más que tocar: ni el proyecto de Xcode ni el bloque `binaries.framework` la
mencionan.

### Qué se pierde

Solo la posibilidad de correr el simulador de iOS en un Mac **Intel**. Los
dispositivos reales (`iosArm64`) y el simulador en Apple Silicon
(`iosSimulatorArm64`) no se ven afectados. Si el Mac de desarrollo es Apple
Silicon, el coste es cero.

> **Verificar en el Mac tras el cambio.** Como toca la configuración de targets
> de iOS, hay que confirmar allí que el proyecto de Xcode sigue compilando y
> enlazando el framework antes de dar la rama por buena. Encaja con la
> verificación de iOS que ya exige el criterio de aceptación 10.

**No es parte de la feature**, es higiene de compilación. Va en esta rama porque
molesta justo ahora, mientras se compila una y otra vez para validar las fases.

> **Aplicado el 18 ago 2026.** Borrada la línea `iosX64()`. Comprobado que no queda
> ninguna referencia en `.kts`, `.toml` ni en el proyecto de Xcode.
> **Pendiente de verificar en el Mac** que Xcode sigue compilando y enlazando el
> framework. Y si tras recompilar siguen saliendo los errores, invalidar la caché
> de configuración de Gradle antes de sospechar del cambio.

---

## 12. El arranque visual: fogonazo negro y "splash" que no era splash

Detectado al probar la fase 1 el 17 ago 2026. Al abrir la app se veía **una
pantalla negra y después un spinner suelto**.

Dos cosas distintas, y ninguna era un fallo de la fase 1:

1. **`Routes.Splash` nunca fue una pantalla de marca.** Es una puerta de arranque
   que decide a dónde va el usuario; lo único que pintaba era
   `FullScreenLoading()`, o sea un `CircularProgressIndicator`. El nombre generó
   la expectativa de un splash que no existía.
2. **El negro lo pintaba Android, no la app.** Es la ventana de inicio, anterior
   al primer fotograma de Compose, y usa el `windowBackground` del tema del
   manifiesto. Ese tema era `@android:style/Theme.Material.Light.NoActionBar`
   —que es claro— pero `androidApp` no declaraba tema propio y **MIUI le aplicaba
   su modo oscuro forzado**, invirtiéndolo a negro (visible en el log como
   `ForceDarkHelperStubImpl ... reason: AppDarkModeEnable`).

**El spinner se queda**: la resolución de sesión no es instantánea. En
instalación limpia hay ida y vuelta a Firebase Auth más dos escrituras en
Firestore (documento de usuario y lista por defecto); con sesión existente, una
lectura de Firestore. En el log medido, la petición de auth salía ~750 ms después
de crearse el ViewModel.

### Aplicado el 17 ago 2026

- `androidApp/src/main/res/values/colors.xml` — `splash_background` (#FFFFFF).
- `androidApp/src/main/res/values/themes.xml` — `AppTheme` con `windowBackground`.
- `androidApp/src/main/res/values-v29/themes.xml` — el mismo tema más
  `android:forceDarkAllowed=false`, que existe desde API 29 y `minSdk` es 24.
- `AndroidManifest.xml` — la aplicación pasa a usar `@style/AppTheme`.
- `SplashScreen.kt` — fondo blanco explícito **del mismo color que
  `splash_background`**, con el icono de la app sobre el spinner.

La clave es que los dos colores coincidan: así no se percibe transición entre la
ventana del sistema y la primera pantalla de Compose. En Android 12 y superiores
el sistema añade además su animación de icono, que también parte de ese
`windowBackground`.

> **Pendiente para iOS.** Allí el mecanismo equivalente es el *launch screen* del
> proyecto de Xcode, que no se ha revisado. Sin eso, la paridad de arranque entre
> plataformas queda a medias. Requiere trabajo en el Mac.

### El tema no bastó: MIUI ignora el `windowBackground`

Probado el 17 ago 2026 con **instalación limpia** (desinstalando primero, así que
no es caché de la ventana de inicio): **la pantalla sigue negra los 3 segundos
enteros.** El tema sí se aplica — `android:theme="@style/AppTheme"` está en el
manifiesto fusionado —, pero MIUI no lo respeta para la ventana de inicio. Y el
modo oscuro forzado tampoco es la explicación: el log dice
`setViewRootImplForceDark: false`.

Se valoró **`androidx.core:core-splashscreen`**, la API oficial que toma el
control de la ventana de inicio en vez de depender de que cada capa de fabricante
respete el `windowBackground`.

> **Descartado el 18 ago 2026.** Medido en release, el arranque baja de un segundo
> (ver sección 13): el negro pasa de tres segundos a un parpadeo. No compensa
> meter una dependencia y configuración por versión de Android para eso. El tema
> propio y el splash con logo se quedan —son correctos y no estorban—, pero aquí
> se para.

---

## 13. El arranque tarda 3,5 segundos

Medido el 17 ago 2026 en el Redmi 9, build debug, en el log del propio usuario:

```
PROCESS STARTED                          12.914
[Koin] init                              15.262   ← 2,3 s después
PerfMonitor longMsg : wall=2459ms        15.301   ← un solo mensaje bloqueó el hilo principal
Choreographer: Skipped 56 frames!        15.487
Davey! duration=1838ms                   16.417   ← primer fotograma
```

Tres segundos y medio del arranque del proceso al primer fotograma. La rama
existe para que el usuario escriba su primer producto en menos de cinco segundos,
y el arranque se comía tres.

Ese `wall=2459ms` cae dentro de `ListaCompraApp.onCreate()`, que hacía todo en el
hilo principal y en serie: `initPlatform`, `MobileAds.initialize()`,
`CrashlyticsKMP.initialize()` e `initKoin` con `Level.DEBUG`.

### Aplicado el 17 ago 2026

- **`ads/AdMobInitializer.kt`** (nuevo, `androidMain`): lanza
  `MobileAds.initialize()` en `Dispatchers.IO` y expone `isInitialized: StateFlow<Boolean>`.
  Google documenta que esa llamada hace E/S en disco y **recomienda invocarla
  desde un hilo secundario** (`developers.google.com/admob/android/optimize-initialization`).
  El `onComplete()` se devuelve al hilo principal: dentro va la precarga del
  intersticial, e `InterstitialAd.load()` exige hilo principal. Hoy esa rama está
  muerta (`INTERSTITIAL_ENABLED = false`), pero se reactivará al extraer la
  librería de anuncios y fallaría de forma intermitente.
- **`ListaCompraApp.kt`**: `androidLogger(Level.DEBUG)` solo en debug; en release
  pasa a `Level.NONE`, que evita ~100 líneas de log en cada arranque.

**Probado y revertido: el banner no espera a la inicialización.** Se llegó a
poner un `isInitialized: StateFlow<Boolean>` en `AdMobInitializer` con el que
`BannerAd` difería su `loadAd()` hasta que la inicialización terminase, siguiendo
la recomendación de Google de esperar al callback. En el dispositivo **el banner
tardaba visiblemente más en aparecer**: la inicialización de AdMob arrastra
WebView y carga de dex dinámico, y en un primer arranque tras instalación limpia
la petición del anuncio no salía hasta ~14 s. Revertido: el banner vuelve a pedir
el anuncio en cuanto se pinta, que es lo que la app ha hecho siempre sin dar
problemas. El beneficio de esperar era teórico; el coste, visible.

### Medición del 18 ago 2026: el cambio funcionó, pero el problema estaba en otro sitio

Primer arranque **tras instalación limpia** (el peor caso: sin perfil de ART
compilado, con verificación de dex completa). No es comparable con los 3,5 s del
día anterior, que era un arranque posterior.

MIUI capturó la pila **en el momento del bloqueo**, y ahí está la respuesta:

```
duration=2525ms seq=3 ... w=110
  at java.lang.Class.newInstance(Native Method)
  at android.app.AppComponentFactory.instantiateApplication
  at android.app.ActivityThread.handleBindApplication
```

El bloqueo está en **instanciar la clase Application**, que ocurre *antes* de que
`onCreate()` se ejecute: es carga y verificación de clases, no código nuestro. Lo
confirma un hueco de 2,35 s sin una sola línea de log.

Y el cambio sí surtió efecto: `Application.onCreate()` completo ocupa ahora
~300 ms, con `Koin Started 42 definitions in 11,089 ms`.

El tiempo se reparte entre cosas ajenas a la app:

| Coste | Origen |
|---|---|
| ~2,35 s | Carga de clases; debug sin R8 y sin perfil compilado |
| 1.775 ms | `Slow Binder: BpBinder transact ... IAccessibilityManager` — el sistema |
| 1.964 ms | `MainActivity onCreate`, buena parte de ellos ese binder |

`ProfileInstaller: Installing profile` aparece **después** del primer fotograma,
así que los arranques siguientes parten de una situación mejor.

### Medido en release el 18 ago 2026: **menos de 1 segundo. Asunto cerrado.**

Los 3,5–7,9 segundos eran un artefacto de la build **debug**: dex sin optimizar,
sin R8 y sin perfil de ART compilado. En release el arranque baja de un segundo y
deja de ser un problema. **No se toca nada más del arranque.**

Lección para la próxima: medir en release **antes** de optimizar. Se invirtió
tiempo persiguiendo un número que no existe en producción, y el `windowBackground`
y la inicialización de AdMob se cambiaron a partir de esa medida engañosa.

Lo aplicado se conserva porque sigue siendo correcto por sí mismo: inicializar
AdMob fuera del hilo principal es lo que recomienda Google, y callar el log de
Koin en release es higiene. Simplemente no era el cuello de botella.

### Hallazgo: WorkManager lo arrastra AdMob, antes de `onCreate()`

`WM-WrkMgrInitializer` aparecía en el log sin que el proyecto use WorkManager. Es
**el propio SDK de AdMob** (`play-services-ads`) quien lo trae, auto-inicializado
vía `androidx.startup.InitializationProvider`, un `ContentProvider` que corre
**antes de `Application.onCreate()`**. Firebase no: usa `JobScheduler`.

Es decir: parte del coste de AdMob es anterior a nuestro código y **este cambio no
lo toca**. Solo se quitaría eliminando ese `<provider>` en el manifiesto, lo que
tiene efectos secundarios. **Sin decidir, deliberadamente.**

### Cómo medir el antes y el después

En logcat, la diferencia entre `PROCESS STARTED` y el primer `Davey! duration=`,
y si sigue apareciendo el `PerfMonitor longMsg : wall=` y con qué cifra.
**Comparar siempre sobre el mismo tipo de build**: debug arranca bastante más
lento que release, así que un debug nuevo contra un release viejo no dice nada.

---

## 14. Guion de pruebas — fases 3 y 3 bis

### Preparación

- **Cuenta A**: una cuenta real ya existente, con **dos listas propias** y productos
  distintos en cada una. Anota sus nombres.
- **Correo B**: un correo que **no** tenga cuenta todavía.
- Proveedor anónimo habilitado en Firebase Console (ya hecho).
- Anota si has publicado ya las reglas nuevas de Firestore: el bloque 5 cambia según eso.

### Bloque 1 — Vincular con un correo nuevo (fase 3)

1. Desinstala e instala. Debe entrar **directo a una lista vacía**, sin login ni anuncio.
2. Añade dos o tres productos.
3. **Antes de seguir**: en Firebase Console → Authentication, anota el **UID** del
   usuario anónimo recién creado. Es la prueba de que el `uid` no cambia.
4. Menú lateral → "Compartir lista".
5. Esperado: sale **"Compartir necesita una cuenta"** con el botón "Crear cuenta".
6. Pulsa "Crear cuenta", rellena con el **correo B** y una contraseña, "Vincular cuenta".
7. **Esperado**: se cierra el formulario y **se abre solo el diálogo de compartir**.
   Los productos siguen estando.
8. En Authentication: **el mismo UID** de antes ahora aparece con el correo B, y ya
   no figura como anónimo.

### Bloque 2 — Vincular con un correo que ya tiene cuenta (fase 3)

1. Desinstala e instala. Añade un producto (tiene que haber algo que perder).
2. Menú lateral → "Compartir lista" → "Crear cuenta" → usa el correo de la **cuenta A**.
3. **Esperado**: aviso *"Ese correo ya tiene una cuenta"* con Cancelar / Continuar.
4. Pulsa **Cancelar**: vuelves al formulario y la lista anónima sigue intacta.
5. Repite y pulsa **Continuar**.
6. **Esperado**: entra en la cuenta A y abre el diálogo de compartir **con las listas
   de A**, no con la anónima. Comprueba nombres y productos.
7. La lista anónima queda huérfana en Firestore. Es lo esperado y estaba avisado.

### Bloque 3 — Regresión del login de siempre

1. Cerrar sesión → entrar con la cuenta A **por correo**. Igual que antes.
2. Cerrar sesión → entrar **con Google**. Igual que antes.
3. Registrar una cuenta nueva desde la pantalla de registro. Igual que antes.
4. **En ninguno debe aparecer el intersticial.**

### Bloque 4 — El crítico: aceptar una lista no borra las tuyas (fase 3 bis)

1. Con la cuenta A, confirma que tienes **dos listas propias con productos**.
2. Desde la cuenta B, comparte una lista con el correo de A.
3. Entra en A, abre notificaciones y **acepta** la invitación.
4. **Esperado: A se queda con TRES listas** — las dos suyas, con sus productos
   intactos, más la compartida.
5. La lista compartida se ve **con su nombre real**, no con un texto genérico.
6. **En la consola de Firestore**: los documentos de `lista-compra` de A **siguen
   todos ahí**. No debe haber desaparecido ninguno.

> Este es el bloque que valida el bug de la sección 10. Si algo falla, es aquí.

### Bloque 5 — Normalización del correo (fase 3 bis)

1. Comparte escribiendo el correo con mayúsculas y espacios: `  Correo@Ejemplo.com `.
2. En Firestore, el documento nuevo de `notifications` debe tener `email` **en
   minúsculas y sin espacios**.
3. La cuenta destinataria recibe la invitación.

### Si algo falla

- Logcat filtrado por el paquete, y `Crashlytics` para las excepciones: los
  repositorios registran todas con `recordException` antes de devolver el fallo.
- Los mensajes que ve el usuario son genéricos a propósito; el detalle real está
  en Crashlytics, no en pantalla.

---

## 15. Trabajo aparte: limpieza de cuentas anónimas huérfanas

**No entra en esta rama.** Anotado el 18 ago 2026 tras verlo en las pruebas del
bloque 2.

### De dónde salen los huérfanos

1. **Caso principal**: un usuario anónimo intenta vincular con un correo que ya
   tiene cuenta, confirma el aviso y entra en la cuenta existente. Su sesión
   anónima queda abandonada, con su documento `usuarios/{uid}` y su lista.
2. Reinstalaciones y borrados de datos de la app: la sesión anónima anterior
   queda inalcanzable para siempre.

### Por qué no se puede resolver desde la app

La única ventana para borrar la cuenta anónima es **antes** de iniciar sesión con
la otra: después ya no se tienen sus credenciales, y Firebase no permite borrar un
usuario ajeno desde el cliente. Tampoco se pueden borrar sus documentos, porque
las reglas exigen `request.auth.uid == userId`.

Y borrar primero abre un riesgo peor: si la contraseña resulta ser incorrecta, el
usuario se queda **sin la lista anónima y sin entrar**. Por eso se descartó.

### Qué habría que borrar

Firestore no borra en cascada, así que hay que ir por partes:

- El usuario de Firebase Authentication.
- Su documento `usuarios/{uid}`.
- Los documentos de `lista-compra` con `owner == uid`.
- **Las subcolecciones `productos` de esas listas**, que si no quedan huérfanas y
  ocupando sitio sin ruta que las alcance.

### Criterio de borrado — el punto delicado

**Nunca por antigüedad a secas.** Una cuenta anónima con listas y productos es la
cuenta de un usuario activo que aún no se ha registrado: borrarla es destruir sus
datos. Es exactamente el motivo por el que se dejó desmarcada la casilla de
"limpieza automática" de Firebase Console (ver sección 7).

El criterio debe ser **anónima Y sin datos que valgan**: sus listas no tienen
ningún producto. Conviene además exigir una antigüedad mínima para no pisar a
alguien que acaba de instalar y todavía no ha escrito nada.

### Cómo

Script con el Admin SDK o Cloud Function programada. `listUsers()` pagina sobre
todos los usuarios; los anónimos se distinguen porque su `providerData` está
vacío. Por cada uno, comprobar sus listas antes de borrar nada.

**Sin urgencia**: las cuentas anónimas no cuestan dinero. Es higiene, no un
problema. Conviene hacerlo cuando haya volumen suficiente para que se note.

---

## 16. El crash al aceptar una lista, y el orden de despliegue

Detectado el 18 ago 2026 probando el bloque 4, **con las reglas nuevas ya
publicadas**. Aceptar una invitación —y también rechazarla— tumbaba la app.

### Dos defectos encadenados, los dos preexistentes

**1. La consulta perdía el filtro por correo.** `UsersDataSource.deleteNotification()`
hacía:

```kotlin
.where {
    "email".equalTo(userEmail)     // ← descartado en silencio
    "listaId".equalTo(listaId)
}
```

El bloque `where { }` de GitLive usa **la última expresión** como filtro
(`firestore.kt:254`), así que el del correo se tiraba. Con las reglas antiguas
daba igual; con las nuevas, una consulta sin filtro por correo no es
demostrablemente segura y Firestore devuelve `PERMISSION_DENIED`.

Arreglado combinando los dos con el `infix fun Filter.and()` de `Filter.kt:371`:

```kotlin
.where {
    "email".equalTo(userEmail) and "listaId".equalTo(listaId)
}
```

Es el **único** `.where { }` multilínea del proyecto; el resto son de una sola
expresión y no están afectados.

**2. El repositorio lanzaba en vez de devolver `Result`.**
`UserRepository.deleteNotification()` hacía `throw e.toUserFailure()`, y
`acceptSharedList()`/`cancelSharedList()` lo llamaban dentro de
`viewModelScope.launch` sin try/catch: excepción sin capturar, app abajo.
Ahora devuelve `Result<Unit>` como el resto, y el fallo se trata como no crítico
—la lista ya está aceptada en ese punto—.

De paso se encontró el **mismo patrón en `deleteAccount()`**: un fallo de
"recently authenticated" habría crasheado igual. También convertido a `Result`.

### La lección: el orden de despliegue

**Las reglas se publicaron antes que la app, y eso rompió producción.** Las reglas
son del servidor y afectan a **todas las versiones instaladas al instante**; una
actualización de la app tarda días en propagarse. Endurecer el servidor antes que
el cliente deja rotos a los usuarios que aún no han actualizado.

El orden correcto, para la próxima:

1. Publicar la app con el cliente ya preparado.
2. Esperar a que se propague.
3. **Entonces** endurecer las reglas.

Mitigación inmediata mientras tanto: devolver el bloque de `notifications` a
`allow read, write, delete: if request.auth != null;`. Se recupera el agujero,
pero un crash visible al aceptar una lista es peor que un agujero que llevaba
años sin explotarse.

---

## 17. La notificación fantasma del usuario anónimo

Detectado el 18 ago 2026: un usuario **anónimo**, recién instalada la app, veía una
notificación diciendo que otra persona le había compartido una lista. Imposible por
definición: no tiene correo, nadie ha podido invitarle.

### Causa: dos fallos preexistentes que se sumaban

1. **`getNotifications()` consultaba con el correo vacío.**
   `auth.currentUser?.email.orEmpty()` da `""` para un anónimo, así que la consulta
   era `where email == ""`.
2. **`shareList()` no validaba el correo.** Solo comprobaba el límite de 5 minutos.
   Alguien pulsó compartir con el campo en blanco y quedó un documento con
   `email: ""` en `notifications`.

Resultado: ese documento le salía a **todos** los usuarios anónimos. Ninguno de los
dos fallos es de esta rama; la sesión anónima solo convirtió un documento inerte en
algo visible para cualquiera que instalase la app.

### Arreglado

- **`getNotifications()`** devuelve un flujo vacío si no hay correo, **sin tocar
  Firestore**. Se usa `userEmail.isEmpty()` y no `isAnonymous()` porque la
  precondición real es tener correo, no el tipo de cuenta.
- **`ShareListaCompraUseCase`** valida con `isValidEmail()`, el mismo mecanismo de
  `UserLoginUseCase`, `UserRegisterUseCase` y `LinkAccountWithEmailUseCase`.
- **`ListaCompraViewModel.shareList()`** hace `trim()` antes de llamar; el
  `lowercase()` sigue donde estaba, en el datasource, sin duplicar.

### Hallazgo colateral: los parámetros estaban cruzados

`ShareListaCompraUseCase.invoke()` declaraba `(nombre, email, listaId)`, que no
coincidía **ni con su único llamante ni con el repositorio**. Funcionaba de pura
casualidad: los dos desajustes de posición se cancelaban entre sí. Al añadir la
validación sobre el parámetro llamado `email` habría validado el `listaId` y roto
el compartir para todo correo válido. Corregido a `(nombre, listaId, email)` en
toda la cadena, ya consistente por nombre y posición en los cuatro saltos.

### Limitación resuelta el 18 ago 2026

`sharedNotificationsFlow` se construye una sola vez, al crear el ViewModel, y
captura el correo de ese instante. Consecuencia concreta con el guardia nuevo:
**un usuario que acaba de vincular su cuenta no verá invitaciones hasta que
reinicie la app**. Antes del guardia el mismo defecto existía en otra forma (seguía
consultando por cadena vacía).

**Arreglado sin coste adicional.** No hizo falta el `flatMapLatest` sobre
`authStateChanged` que parecía necesario: `sharedNotificationsFlow` se colecciona
en un **único** sitio, y `loadUserDataSuspending()` ya cancela ese job antes de
volver a suscribirse. Lo único congelado era la consulta. Bastó convertir el `val`
en `private fun notificationsFlow()`, de modo que cada suscripción vuelva a leer el
correo actual.

**El consumo de Firestore es idéntico**: sigue habiendo un solo listener activo en
cada momento. La preocupación por el gasto era razonable pero no aplicaba: aunque
se hubiera ido por `authStateChanged`, ese listener dispara al iniciar y cerrar
sesión —no en la renovación horaria del token, que es `idTokenChanged`—, o sea una
a tres veces por sesión, con cero, una o dos lecturas cada vez, contra un nivel
gratuito de 50.000 lecturas diarias.

---

## 18. Trabajo aparte: avisos push al compartir una lista

**No entra en esta rama.** Anotado el 18 ago 2026.

Hoy las invitaciones se ven **solo con la app abierta** y en la pantalla principal:
`UsersDataSource.getNotifications()` monta un listener de Firestore (`.snapshots`)
que empuja en tiempo real mientras está activo. Si el destinatario tiene la app
cerrada, no se entera hasta que la abre.

Para que llegue un aviso al móvil con la app cerrada hace falta **Firebase Cloud
Messaging**: una Cloud Function que dispare al crearse un documento en
`notifications` y mande el push al destinatario. Implica además guardar el token
FCM por usuario y pedir permiso de notificaciones en Android 13+.

Encaja de forma natural con **el bucle de invitación por correo a quien no tiene la
app**, que ya figura en la sección 4 como fuera de alcance. Los dos van del mismo
problema: hoy una invitación solo existe dentro de la app.

---

## 19. Cadenas en duro preexistentes — HECHO el 18 ago 2026

Listado durante la auditoría de la fase 4 y **aplicado después**, a petición del
usuario. Lo que se hizo:

- **Menú lateral**: "Mis listas", "Compartir lista", "Cerrar sesión", "Eliminar
  cuenta" y "Versión" pasan a recurso. Hallazgo: tres de esas claves **ya existían**
  en los tres idiomas y nadie las estaba usando — el literal estaba duplicado al
  lado. Ahora se consumen. El número de versión se queda fuera del recurso porque
  el proyecto no tiene ningún precedente de cadenas con parámetros.
- **Los 10 títulos de alerta del ViewModel y el aviso de los 5 minutos**, que la
  fase 4 había dado por bloqueados. **No lo estaban**: Compose Multiplatform expone
  `suspend fun getString(resource: StringResource): String` en
  `org.jetbrains.compose.resources` (verificado en las fuentes de
  `components-resources:1.11.1`), y todos esos puntos ya estaban en contexto
  suspend. Corregida de paso la errata "invitacion".
- **`app_name`** añadido a `values-es/` y `values-ca/`, y arreglado el apóstrofo sin
  escapar de `values-ca/strings.xml:27`.

### Lo que sigue en duro, a propósito

- **`UsersDataSource.getListas()`**, respaldo `"Lista de la compra"`: es capa de
  datos, y meter recursos de UI ahí es peor que la cadena en duro.
  > Hay una salida limpia, descrita pero no implementada: el **mismo** respaldo
  > existe una capa más arriba, en `ListaCompraViewModel.loadUserDataSuspending()`,
  > ya en contexto suspend. Ese sí podría pasar a `getString()` sin tocar el
  > datasource.
- `contentDescription = "Icono menú"` genéricos, las cadenas de `HomeContent.kt`
  ("Abrir menú", "Borrar lista", "Agregar producto", el diálogo de confirmación) y
  los botones de depuración "Forzar crash / non-fatal". No estaban en el inventario.

### Inventario original, para referencia

### Menú lateral (`MenuLateral.kt`)
Líneas 90, 112, 127, 170 y 210: "Mis listas", "Compartir lista", "Cerrar sesión",
"Eliminar cuenta" y "Version v…". Son las más visibles: un usuario en inglés o
catalán ve ese menú **medio traducido**, porque la entrada nueva
("Iniciar sesión / Crear cuenta") sí está localizada y las de al lado no.

### Títulos de alerta (`ListaCompraViewModel.kt`)
Preexistentes: "Error al obtener el Usuario", "Error al aceptar la invitacion"
(con su errata), "Error al compartir", "Error al actualizar", "Error al agregar
producto", "Error al eliminar", "Error al eliminar lista", y el aviso de los cinco
minutos.

Nuevos de esta rama y **no localizados a propósito**: "Error al vincular la
cuenta", "Error al iniciar sesión", "Error al borrar la cuenta".

> **El motivo por el que no se tocaron es de arquitectura, no de pereza.** Se
> generan en el ViewModel, fuera de un contexto `@Composable`, y el proyecto no
> resuelve recursos fuera de Compose: cero usos de `getString(Res.string…)` en todo
> `commonMain`. Localizarlos exige introducir ese mecanismo por primera vez, que es
> una decisión de diseño y merece su propia rama.

### Capa de datos (`UsersDataSource.getListas()`)
El respaldo `"Lista de la compra"`, mismo problema: la capa de datos no tiene acceso
a los recursos de Compose.

### Desajustes antiguos entre idiomas
- **`app_name`** solo existe en `values/` (inglés); falta en `values-es/` y
  `values-ca/`. Ya era así en `main`.
- **`values-ca/strings.xml:27`** tiene un apóstrofo sin escapar
  (`T'enviarem`, frente al `S\'ha` del resto del fichero). **No rompe el build** —la
  app compila hoy con él y los recursos de Compose no pasan por AAPT—, es solo
  inconsistencia de estilo.

---

## 20. Trabajo aparte: sacar `interactions` de `composables/`

**No entra en esta rama.** Anotado el 18 ago 2026 al revisar el código.

Hoy la tríada MVI de cada feature vive en `ui/composables/interactions/`:

```
home/ui/composables/interactions/       ListaCompraEffect / Event / State
login/ui/composables/interactions/      AuthEffect / Event / State
                                        SplashEvent / State
mislistas/ui/composables/interactions/  MisListasEvent / State
```

**Debería estar en `ui/interactions/`.** Un `State`, un `Event` y un `Effect` no
son composables: son el contrato de la pantalla, consumido tanto por la UI como
por el ViewModel. Colgarlos de `composables/` mezcla dos cosas distintas y sugiere
una dependencia que no existe.

### Por qué no se hizo aquí

La ubicación es **preexistente**, no la introdujo esta rama: de las diez tríadas,
ocho ya estaban ahí y solo `SplashEvent`/`SplashState` son nuevas —puestas ahí
siguiendo la convención que documentaba la propia sección 5 de este plan—.

Mover solo las dos nuevas dejaría `SplashState` en un sitio y `AuthState`, su
vecina del mismo feature, en otro: peor que cualquiera de las dos opciones puras.
Y mover las tres carpetas enteras toca **38 ficheros** que importan de ese paquete,
lo que engorda mucho el diff de una rama que va de otra cosa.

### Cómo hacerlo

Mecánico pero amplio: mover los diez ficheros, cambiar su declaración `package` y
actualizar los imports en los 38 consumidores. En su propio commit, para que el
cambio de convención se lea limpio y sea fácil de revertir si algo se tuerce.

> Actualizar también la sección 5 de este plan, que hoy documenta la ubicación
> antigua como si fuera la buena.

---

## 21. Mejora: sacar la vinculación de cuenta a su propia ruta

**No entra en esta rama.** Anotado el 18 ago 2026 al revisar el código, tras
preguntarse por qué `ListaCompraEvent` había crecido tanto.

### El síntoma

`ListaCompraEvent` sumó doce eventos nuevos de login y vinculación. La ubicación
es **correcta según la convención del proyecto** —la tríada MVI es por pantalla, no
por tema, y esa UI la pinta `HomeScreen` y la gobierna `ListaCompraViewModel`—,
así que moverlos a `AuthEvent` sería peor: dejaría UI de Home mandando eventos a un
ViewModel que no la controla.

### El problema real

No es el fichero de eventos, es el ViewModel:

| | Antes | Ahora |
|---|---|---|
| `ListaCompraViewModel` | 471 líneas | **611** |
| Casos de uso inyectados | | **16** |
| Eventos en `ListaCompraEvent` | | **43** |

`AuthViewModel` sigue en 157 líneas, sin tocar. La vinculación de cuentas y el
inicio de sesión acabaron dentro del ViewModel del Home, que ya era el mayor del
proyecto.

### Qué mover y qué no

**Se quedan en Home (4)**: `OnLoginFromMenuClick`, `OnConfirmLoginDataLoss`,
`OnCancelLoginDataLoss`, `OnLoginFromEmptyListClick`. Son "el usuario ha pulsado
algo en el menú o en la lista vacía" y solo navegan.

**Se van (8)**: los dos del diálogo de "compartir necesita cuenta", los cuatro del
formulario de vinculación y los dos del correo ya registrado. Son una pantalla
propia disfrazada de diálogo.

### Cómo

Una ruta `Routes.LinkAccount` con su `LinkAccountViewModel` y su tríada, igual que
ya son rutas Login, Registro y ForgotPassword. Home se queda con **un** evento
—"llévame a vincular"— en vez de ocho, y `ListaCompraViewModel` suelta
`linkAccountWithEmailUseCase` y `userLoginUseCase`.

> **Hacerlo después de validar la rama en dispositivo**, no antes: mezclar "esto no
> funciona" con "esto lo he movido de sitio" complica el diagnóstico.

---

## 22 bis. Sacar Splash a su propia feature

**No entra en esta rama.** Propuesto por el usuario el 18 ago 2026 al revisar el
código, y **forma parte de la misma pasada de arquitectura** que las secciones 20 y
21 (ver más abajo por qué).

### El problema

`login/` alberga hoy **dos ViewModels**: `AuthViewModel` y `SplashViewModel`.

Que `AuthViewModel` cubra Login, Registro y Recuperar contraseña tiene sentido: son
un mismo flujo de autenticación. Splash no pertenece a ese flujo — resuelve **con
qué sesión arranca la app**, que es otra responsabilidad. Y tiene todo lo de una
feature completa: tríada MVI propia, ViewModel, pantalla y caso de uso que **solo
usa él** (`ResolveSessionUseCase`, comprobado con grep).

### Qué se mueve

```
login/ui/SplashViewModel.kt                       ->  splash/ui/
login/ui/screens/SplashScreen.kt                  ->  splash/ui/screens/
login/ui/composables/interactions/SplashState.kt  ->  splash/ui/.../interactions/
login/ui/composables/interactions/SplashEvent.kt  ->  splash/ui/.../interactions/
```

Más actualizar los imports en `NavigationWrapper.kt` y `NetworkModule.kt`.

### Qué NO se mueve, y por qué

`SignInAnonymouslyUseCase` y `ResolveSessionUseCase` **se quedan en
`login/domain/usecase/`**. Envuelven a `UserRepository`, que es de `login`, y el
proyecto ya tiene ese precedente: `home` consume `GetUserUseCase` y `LogOutUseCase`
de `login` sin owner propio. Moverlos dejaría a una feature accediendo directamente
al repositorio de otra, un salto más profundo del que da nadie hoy.

---

## La pasada de arquitectura: 20 + 21 + 22 bis, juntas

Las tres se solapan y hacerlas por separado significa **mover los mismos ficheros
dos veces**:

- La **20** cambia dónde vive  en todas las features.
- La **21** crea una feature nueva para la vinculación de cuenta.
- La **22 bis** crea una feature nueva para Splash.

Si primero se crean  y la de vinculación con la estructura actual, y
después se aplica la 20, hay que volver a tocarlas. **Una sola pasada**, con la
convención nueva ya decidida.

> **Después de verificar iOS**, no antes: no conviene mover paquetes bajo los pies
> de una rama que está a punto de validarse del todo.

---

## 22. Guion de pruebas definitivo (sustituye al de la sección 14)

La sección 14 se escribió antes del crash, de la validación al compartir, de la
localización y del cambio de targets. Este lo cubre todo.

### Preparación

- Reglas de Firestore en la **versión permisiva** (rollback ya hecho).
- Proveedor anónimo habilitado.
- **Borrar de `notifications` el documento con `email` vacío** — es el que provocaba
  la notificación fantasma.
- **Cuenta A**: real, con **dos listas propias** y productos distintos en cada una.
- **Correo B** y **correo C**: dos correos sin cuenta.

### Bloque 1 — Compila

1. Compilar debug. **Si falla, parar**: la limpieza de comentarios tocó 22 ficheros.
2. Comprobar que **ya no salen los cuatro errores de `iosX64`**. Si siguen,
   invalidar la caché de configuración de Gradle antes de sospechar del cambio.

### Bloque 2 — Arranque e instalación limpia

3. Desinstalar e instalar. **Entra directo a una lista vacía**: sin login, sin
   anuncio, con el logo sobre fondo blanco mientras carga.
4. **No aparece ninguna notificación.** (Antes salía una invitación ajena.)
5. Añadir dos productos, cerrar la app del todo y volver a abrir: siguen ahí.

### Bloque 3 — Vincular con un correo nuevo

6. Anotar el **UID** del usuario anónimo en Firebase Console → Authentication.
7. Menú lateral → "Compartir lista" → sale *"Compartir necesita una cuenta"*.
8. "Crear cuenta" → el formulario. **El botón pone "Continuar" en una sola línea.**
   El texto dice que la lista **se trasladará** a la cuenta nueva.
9. Rellenar con el **correo B** → Continuar.
10. **Se abre solo el diálogo de compartir** y los productos siguen ahí.
11. En Authentication: **el mismo UID**, ahora con el correo B y sin marca de
    anónimo.

### Bloque 4 — Validación al compartir

12. Con el diálogo abierto, pulsar compartir **con el campo vacío** → error, y **no**
    se crea ningún documento en `notifications`.
13. Probar con texto que no sea un correo (`hola`) → mismo error.
14. Compartir con el **correo C**, escribiéndolo con mayúsculas y espacios:
    `  Correo@Ejemplo.com `. En Firestore el campo `email` queda **en minúsculas y
    sin espacios**.

> El paso 14 valida además que el reordenamiento de parámetros de
> `ShareListaCompraUseCase` no rompió nada.

### Bloque 5 — Recibir una invitación en la misma sesión

15. Sin cerrar la app, crear a mano en la consola de Firestore un documento en
    `notifications` con `email` = correo B, `nombre` = "Prueba", `listaId` = el id de
    cualquier lista.
16. **La notificación aparece en la app sin reiniciar.** Esto valida que el flujo se
    rehace al vincular la cuenta.

### Bloque 6 — El crítico: aceptar y rechazar

17. Con la **cuenta A**, confirmar que tiene dos listas propias con productos.
18. Compartir una lista con el correo de A desde la otra cuenta.
19. En A, **rechazar** la invitación. **No debe crashear.**
20. Volver a compartir y ahora **aceptar**. **No debe crashear.**
21. **A se queda con TRES listas**: las dos suyas con sus productos intactos, más la
    compartida, **con su nombre real**.
22. En la consola de Firestore: **no ha desaparecido ningún documento** de
    `lista-compra`.

> Los pasos 19 y 20 son los que validan el crash. Iban por el mismo camino roto.

### Bloque 7 — Regresiones

23. Cerrar sesión → entrar con la cuenta A **por correo**. Igual que antes.
24. Cerrar sesión → entrar **con Google**. Igual que antes.
25. Registrar una cuenta nueva desde la pantalla de registro.
26. **Borrar una cuenta** (usa una de prueba). Se tocó sin haberlo pedido.
27. **En ninguno de los anteriores aparece el intersticial.**
28. Provocar un error a propósito —compartir sin red, por ejemplo— y comprobar que
    **la alerta sale con su título**. Los títulos ahora vienen de recursos.

### Bloque 8 — Idiomas

29. Cambiar el idioma del móvil a inglés y abrir el menú lateral: **todas** las
    entradas en inglés, sin mezcla.
30. Repetir en catalán.
31. Una lista sin nombre se llama **"Lista de la compra"**, no "Lista 1".

### Bloque 9 — iOS (en el Mac)

32. **Que Xcode compile y enlace el framework** tras quitar `iosX64()`.
33. Repetir los bloques 2, 3 y 6.
34. Comprobar el arranque: iOS no tiene *launch screen* revisado, así que anotar qué
    se ve.

### Si algo falla

Logcat filtrado por el paquete, y Crashlytics para las excepciones: los repositorios
registran todas con `recordException` antes de devolver el fallo.


---

## 23. Lo que salió en iOS: el banner negro y el botón partido

Probado en el Mac el 18 ago 2026. Compila y enlaza sin problemas tras quitar
`iosX64()` — el Mac es Apple Silicon, así que perder el simulador Intel no cuesta
nada. La persistencia de sesión en Keychain (criterio 6) también quedó validada:
la app entra directa sin teclear la contraseña.

Salieron dos cosas.

### 23.1 El banner tarda 10-15 s y mientras se ve un rectángulo negro

Dos problemas distintos que se veían como uno.

**El negro.** `BannerAd` de iOS fuerza `height(50.dp)` sobre un `UIKitView` para
que Compose le reserve sitio (un `UIView` plano no tiene `intrinsicContentSize`).
Ese hueco lo pinta UIKit, no Compose, y el contenedor no tenía `backgroundColor`,
así que hasta que el anuncio llega se ve negro. En Android no pasa porque el
`AdView` va sin altura forzada y el espacio simplemente no existe hasta que carga.

Arreglo: dar al contenedor el color de fondo de la pantalla (`SecondaryBlue`, el
mismo del `Box` de `HomeContent` y del espaciador que va justo debajo). El hueco
sigue reservado — así no salta la lista cuando entra el anuncio — pero pasa a
leerse como margen en vez de como un agujero.

**La espera al ir y volver.** El `factory` de `UIKitView` se ejecuta cada vez que
el composable entra en composición. Cada regreso a la lista creaba un `UIView`
nuevo, mandaba `AdMobLoadBannerRequested` otra vez y arrancaba una petición de red
desde cero. De paso registraba dos observers más, que solo se retiran cuando llega
la notificación correspondiente.

Arreglo: un mapa `bannerContainers` a nivel de fichero, indexado por unidad de
anuncio. Si ya hay contenedor, se reutiliza (con `removeFromSuperview()` antes,
porque la instancia anterior de `UIKitView` lo dejó colgando de su jerarquía). Si
la carga falla se borra del mapa, de forma que la siguiente navegación reintenta
en vez de quedarse con un hueco vacío para siempre.

> **Efecto secundario a tener presente:** en el camino de reutilización no se
> registran observers nuevos, así que `onAdLoaded` y `onAdFailedToLoad` de esa
> segunda composición no se llaman. Hoy da igual — `HomeContent` los deja por
> defecto y mide la altura con `onSizeChanged` —, pero si alguien empieza a
> depender de esos callbacks, esto hay que revisarlo.

**Lo que no arregla ninguno de los dos:** la espera de la primera carga en frío.
`USE_TEST_ADS = false`, así que son unidades de producción, y con el tráfico que
tiene la app el *fill* de AdMob tarda lo que tarda. Lo que se corrige es que la
espera no se vea como un rectángulo negro y que no se repita en cada navegación.

### 23.2 "Crear cuenta" partido en dos líneas en un iPhone X

En `ShareRequiresAccountDialog` los dos botones van con `weight(1f)` dentro de una
`Row` con 20.dp de padding y 8.dp de separación. Con el `contentPadding` por
defecto de Material3 (24.dp por lado) al texto le quedan unos 90 dp en una
pantalla de 375 pt, y "Crear cuenta" no cabe. En inglés, "Create account", menos
todavía.

Es el mismo problema del paso 9 en Android, donde "Continuar" se arregló con
`maxLines = 1`. Aquí `maxLines = 1` a secas no basta: recortaría el texto. Así que
va con `contentPadding = PaddingValues(horizontal = 8.dp)` **y** `maxLines = 1` en
los dos botones, que deja unos 130 dp de ancho útil — sitio de sobra para las tres
traducciones.

### 23.3 El arranque de iOS empezaba en negro

`iosApp/iosApp/Info.plist` tenía `UILaunchScreen` como diccionario vacío. Sin
`UIColorName`, iOS pinta el *launch screen* con `systemBackground`, que **sigue la
apariencia del sistema**: blanco en modo claro y **negro en modo oscuro**. Con el
móvil en oscuro se veía un negro y, justo después, el splash de Compose con logo y
spinner sobre blanco. El salto de negro a blanco cantaba.

En Android esto ya estaba resuelto por el otro lado: tema propio con
`windowBackground` blanco antes de que arranque nada.

Arreglo: un colorset `LaunchBackground` en `Assets.xcassets`, blanco fijo y sin
variante oscura, y `UILaunchScreen` apuntando a él con `UIColorName`. El arranque
queda blanco en las dos apariencias y enlaza con el splash sin salto.

> Se descartó `UIUserInterfaceStyle = Light` en `Info.plist`, que también lo
> arreglaría. Fuerza la apariencia clara en **toda** la app, incluido el teclado,
> las alertas del sistema y la hoja de compartir. Es una decisión de producto más
> grande que un color de arranque, y la interfaz ya es clara por sus propios
> colores. Si algún día molesta ver componentes del sistema en oscuro, esa es la
> palanca.

Queda **pendiente de decidir**: el *launch screen* sigue sin logo. El logo lo pone
el splash de Compose, ya en marcha la app. Si se quiere que aparezca desde el
primer fotograma, hay que añadir un imageset y `UIImageName` al mismo diccionario.


---

## 24. Graphify en el Mac: instalado, y lo que hay que saber

Instalado el 19 ago 2026 en el Mac. Tres cosas cambiaron respecto a lo que hay en
Windows, y conviene tenerlas presentes antes de tocar nada allí.

**El paquete y el directorio se renombraron.** El paquete de npm es ahora
`@sentropic/graphify` (el antiguo `graphifyy` solo redirige), y el estado ya no va
en `graphify-out/` sino en **`.graphify/`**. La propia herramienta llama "legacy" al
directorio viejo y trae `graphify migrate-state` para convertirlo. En Windows hay
que actualizar el paquete y migrar, o el `CLAUDE.md` nuevo buscará una ruta que
allí no existe. Ambos directorios están en `.gitignore`: la herramienta considera
`.graphify` estado de ejecución y recomienda no commitearlo.

**Tree-sitter no sabe leer este proyecto.** La versión 0.17.1 trae gramáticas para
C, C++, Go, Java, JS, PHP, Python, Ruby, Rust y TypeScript. **Kotlin y Swift no
están.** De los 136 ficheros de código del repo, la extracción AST resuelve dos —
el plugin JS de OpenCode — y falla en los otros 134. Un `graphify update .` a secas
produce un grafo de 242 nodos de los que 240 son commits de git: inservible.

Por eso el grafo bueno se construye por el **pase semántico**, metiendo el Kotlin y
el Swift en los lotes que normalmente solo llevan documentos. Son subagentes
leyendo el código, no un parser. Resultado: **439 nodos, 840 aristas, 20
comunidades**, con `graph.json` en 724 KB. Las 24 imágenes del corpus se dejaron
fuera a propósito: son el mismo icono de launcher a distintas resoluciones.

**Consecuencia de que lo escriba un modelo y no un parser: hay que verificar.** Al
revisar el grafo apareció una arista falsa, `addSharedList -> deleteOwnerList`
marcada como `EXTRACTED` y con origen en este mismo plan. El subagente había leído
la descripción del bug **ya arreglado** y la codificó como comportamiento actual.
Se eliminó y se comprobó que no había más aristas de código afirmadas desde
documentación. Si alguna vez el grafo dice algo raro sobre el código, esa es la
sospecha primera.

> **Orden que importa, aprendido rompiéndolo:** las descripciones se ingieren
> **después** de la última reconstrucción del grafo. `graphify describe` borra los
> ficheros de respuesta al ingerirlos, y reconstruir `graph.json` después las
> descarta: se pierden las dos copias a la vez. Reconstruir primero, describir
> después.

### 24.1 Cómo se actualiza el grafo tras tocar código

`scripts/graphify-refresh.sh`, en dos pasos:

```
./scripts/graphify-refresh.sh plan     # qué ficheros han cambiado
./scripts/graphify-refresh.sh build    # fusiona, reconstruye y restaura descripciones
```

Entre uno y otro, el asistente extrae los ficheros que liste `plan` y deja un
fragmento en `.graphify/chunks/out-00.json`.

**Cuándo se lanza: al terminar una feature, o cuando el usuario lo pida.** No tras
cada cambio de código. La extracción incremental es barata, pero cada
reconstrucción renumera las comunidades y obliga a repasar sus nombres a mano —
en la sesión del 19 ago 2026 se pagó ese peaje cuatro veces seguidas. Entre
actualizaciones el grafo va por detrás del código, y eso es aceptable siempre que
se diga cuando la respuesta depende de algo recién tocado.

La caché semántica va por **hash de contenido**, así que la actualización es
incremental de verdad: en la prueba, 145 de 148 ficheros salieron de caché y solo
se re-extrajeron los 3 editados. Un cambio de un par de ficheros es un subagente,
no siete.

El script resuelve además las dos trampas que costaron caras el 19 ago 2026:

- **Guarda las descripciones antes de reconstruir** en
  `.graphify/descriptions-cache.json` y las repone en los lotes de respuesta
  después. Sin eso cada reconstrucción obliga a reescribir 439 descripciones,
  porque `graphify describe` borra los ficheros de respuesta al ingerirlos.
- **Detecta que `graph.json` no se ha actualizado.** Si el grafo nuevo tiene menos
  nodos que el guardado, graphify se niega a sobrescribir — red de seguridad útil,
  pero sin comprobarlo el script cantaría victoria mientras el fichero sigue igual.
  Ahora sale por código 2 con el aviso.

Lo que no se automatiza: las descripciones de nodos nuevos (quedan listados en
`.graphify/descripciones-pendientes.txt`) y los nombres de comunidad, que hay que
revisar porque **Louvain las renumera en cada reconstrucción**.

### 24.2 Trampa de `file_type`: `concept` y `rationale` se descartan en silencio

El esquema que documenta la skill de graphify admite
`code|document|paper|image|concept|rationale`. Es mentira a medias:
`validateSemanticFragment` acepta los seis, pero **`sanitizeSemanticFragment`
descarta `concept` y `rationale` sin decir nada**. Comprobado uno a uno el 19 ago
2026.

Eso explica los 59 nodos que se evaporaron en la primera fusión (496 → 437): eran
los conceptos que los subagentes habían creado siguiendo el esquema documentado.

**Un concepto sacado de un documento va como `document`.** Con ese cambio, los 13
nodos de decisiones del plan — sesión anónima, cuenta solo al compartir, el bug
destructivo, el pendiente de `nombresListas`, el orden de despliegue, los arreglos
de iOS — entraron sin problema y quedaron enlazados al código que explican.

El grafo pasó de 439 a **447 nodos, 858 aristas y 19 comunidades**, con las 447
descripciones puestas.

> **Segunda trampa, ya resuelta en el script:** la caché no puede reproducir los
> nodos del AST ni los que no tienen fichero de origen válido — colores del tema
> cuyo `source_file` era un directorio, composables sin ruta. Desaparecían en cada
> reconstrucción. El script los conserva en `.graphify/nodes-extra.json` y los
> vuelve a inyectar; fueron 11.

### 24.3 Por qué dejamos de mantener descripciones y etiquetas

Decidido el 19 ago 2026, con los números delante.

**Lo que costó construir el grafo:** los siete subagentes de extracción reportaron
**515.717 tokens**. Sumando los agentes de descripciones que murieron a medias y
las 449 descripciones escritas —y reescritas tras perderlas—, el día se fue en
algo entre 700.000 y 900.000 tokens.

**Lo que ahorra consultarlo:** `graphify explain` devuelve 44 palabras; una `query`
con presupuesto 700, unas 175. Pero la comparación justa no es contra leer el
fichero entero, sino contra lo que se hace de verdad:

```
sed -n '/suspend fun addSharedList/,/^    }$/p' UsersDataSource.kt
```

Eso son 20 líneas, unos 250 tokens. **Lo mismo que la consulta al grafo.**

**El dato que zanjó la discusión:** el proyecto son 9.358 líneas de Kotlin y Swift,
y en toda la sesión del 19 ago el grafo no aportó ni un hallazgo. El banner negro,
el botón partido, el arranque en negro y el pendiente de `nombresListas` salieron
de `grep` y `sed`. Para amortizar 516.000 tokens a ~3.000 ahorrados por consulta
harían falta unas 170 consultas útiles, y en un código de este tamaño no se dan.

**Decisión: se conserva el grafo, se deja de mantener la capa cara.** Descripciones
y etiquetas eran el 80% del coste recurrente y solo alimentan el studio visual, que
no se usa. `query`, `path` y `explain` funcionan igual sin ellas. Las 449
descripciones ya escritas se reponen gratis en cada reconstrucción desde
`.graphify/descriptions-cache.json` — es copia de ficheros, no cuesta tokens — y
los nodos nuevos se quedan sin ella a propósito.

Los nombres de comunidad se pierden en cada reconstrucción porque Louvain las
renumera; reutilizarlos a ciegas pegaría el nombre a un grupo distinto. Salen como
`Community N` salvo que alguien los escriba a mano con
`./scripts/graphify-refresh.sh etiquetas`.

### 24.4 Corrección: el criterio anterior estaba mal planteado

La primera versión de 24.3 decía que si en unas semanas no se lanzaba ninguna
consulta, tocaba quitar el grafo. **Ese criterio es circular**: en la sesión del 19
ago no se consultó el grafo porque se tiró de `grep` por costumbre, no porque el
grafo fallara. Medir una herramienta sin usarla y concluir que no sirve no vale.

Así que se midió de verdad, con las preguntas reales de esa sesión:

| Consulta | Resultado |
|---|---|
| `query "por que el banner se ve negro en iOS"` | **Falla.** Devuelve `HomeContent`, `MisListasContent`, `PrimaryBlue`. No aparecen `bannerContainers` ni `createAdMobBannerView`, que son la respuesta. |
| `query "donde se guarda el nombre de las listas"` | **Falla.** Devuelve ViewModels genéricos, no `getListas()` ni `addNewLista()`. |
| `query "nombresListas"` (término exacto) | **Acierta.** El nodo correcto sale el tercero. |
| `path "addSharedList()" "getListas()"` | **Acierta**, en una línea: `--shares_data_with-->`. |
| `explain "<nodo conocido>"` | **Acierta**, 44 palabras con todas sus conexiones. |

**Conclusión: `query` ordena por conectividad, no por significado.** Es una
herramienta de recorrido de grafo, no un buscador semántico. Para llegar a un nodo
hace falta saber ya cómo se llama — la misma habilidad que elegir un buen patrón de
`grep`, y con un coste parecido.

**El reparto correcto, y lo que está escrito en `CLAUDE.md`:**

- *"¿Qué toca X? ¿Qué se rompe si lo cambio? ¿Cómo conecta con Y?"* → grafo, sin
  discusión. `explain` y `path` contestan lo que `grep` no puede.
- *"¿Por qué pasa esto?"*, o cuando aún no sabes el nombre de lo que buscas →
  `grep` de entrada, grafo para expandir.

El criterio para decidir si el grafo se queda no es cuántas veces se consulta, sino
**cuántas veces contesta sin tener que caer en leer ficheros**. Eso solo se sabe
usándolo primero, que es lo que ahora manda `CLAUDE.md`.

### 24.5 Erosión de aristas al refrescar, y cómo se frenó

Detectado el 19 ago 2026 al refrescar el grafo tras tocar once ficheros de código:
los nodos subieron a 453 pero las aristas **cayeron de 851 a 755**.

**Causa:** cuando el fragmento de re-extracción se reconstruye a partir del propio
`graph.json` — el atajo que se usa cuando la estructura no ha cambiado, solo el
contenido —, es fácil incluir únicamente las aristas cuyos **dos extremos** están en
los ficheros re-extraídos. Las que cruzaban hacia ficheros sin tocar se quedaban
fuera y desaparecían. Cada refresco erosionaba un poco más el grafo.

Las cadenas importantes aguantaron — `path "ListaCompraViewModel"
"UsersDataSource.addSharedList()"` sigue resolviendo en 3 saltos — pero aparecieron
tres nodos huérfanos nuevos, todos duplicados de baja importancia.

**Arreglo:** el paso de rescate del script conserva ahora también las aristas del
grafo anterior entre nodos que siguen existiendo, no solo las de los nodos
rescatados.

> **Contrapartida, a sabiendas:** una arista que desaparezca de verdad del código no
> se borra sola; sobrevive mientras sus dos extremos existan. Para limpiar de raíz
> hace falta una extracción completa. Es el intercambio correcto: erosionar el grafo
> en cada refresco es peor que arrastrar alguna arista de más.

### 24.6 Las reglas endurecidas funcionan: mi análisis fue demasiado pesimista

Publicadas y validadas en iOS el 19 ago 2026, con la versión estricta.

Yo había avisado de un riesgo alto: `getNotifications()` no lee un documento, hace
una **query** con listener (`where email == userEmail`), y en Firestore las reglas
no filtran — el motor tiene que demostrar que la restricción de la query garantiza
la regla. Como la regla aplica `resource.data.email.lower().trim()`, di por probable
que no supiera casarlo con una igualdad exacta y denegara la query entera. Con el
agravante de que `notificationsFlow()` tiene un `.catch { emit(emptyList()) }`, así
que el fallo habría sido **silencioso**: invitaciones que dejan de aparecer, sin
error, para todo el mundo.

**No ocurrió.** Las cuatro comprobaciones pasaron: la invitación llega sola, se
acepta y desaparece, y un usuario anónimo no ve ninguna. El motor de reglas sí
resuelve la comparación con transformaciones sobre este tipo de query.

Queda escrito para que nadie "arregle" un problema que no existe: **la regla
estricta con `.lower().trim()` es la que está en producción y funciona.** La versión
simplificada, con comparación exacta, sigue siendo una alternativa válida si algún
día las transformaciones dan guerra, pero no hace falta.

> Y una lección de método: el fallo previsto era plausible y bien razonado, pero la
> prueba costaba dos minutos y la especulación habría costado un cambio de reglas
> innecesario. Con producción delante, medir antes que deducir.
