# Traspaso a la sesión del Mac — verificación de iOS

Rama: **`feature/onboarding-sin-registro`**
Fecha del traspaso: 18 ago 2026
Trabajo hecho en Windows; **Android validado, iOS sin tocar**.

---

## 0. Antes de nada: el código no está commiteado

Todo el trabajo está **en el árbol de trabajo, sin commitear**. `HEAD` sigue en
`3a305fe`. Para que el Mac tenga el código hay que **commitear y subir la rama**
primero — decisión del usuario, no la tomes por tu cuenta.

Ficheros de referencia en la raíz del repo:

| Fichero | Qué es |
|---|---|
| `PLAN-ONBOARDING-SIN-REGISTRO.md` | El plan completo, con el porqué de cada decisión. **La memoria de la rama.** |
| `PRUEBAS-ONBOARDING.md` | Guion de pruebas con los resultados de Android ya marcados |
| Este fichero | Lo que falta: iOS |

---

## 1. Qué hace esta rama

La app obligaba a registrarse antes de ver nada, y encima metía un anuncio a
pantalla completa después del login. Con 32% de conversión en la ficha de Play
pero **2 usuarios retenidos a 7 días en 5 meses**, el problema era el primer
arranque.

Ahora:

- **Sesión anónima automática** (`signInAnonymously`) al abrir por primera vez. Se
  entra directo a una lista vacía, sin login y sin anuncio.
- La cuenta se pide **solo al compartir**, y se vincula con `linkWithCredential`
  **conservando el mismo `uid`**: la lista no se pierde ni se migra.
- **Arreglado un bug destructivo**: aceptar una lista compartida borraba las listas
  propias del usuario (de Firestore, no solo la referencia).

Casi toda la lógica vive en `commonMain`, así que iOS debería funcionar. Eso es
justo lo que hay que comprobar.

---

## 2. Estado: qué está validado y qué no

**Android: 35 de 39 pasos en verde**, incluido el bloque crítico del bug
destructivo. Detalle en `PRUEBAS-ONBOARDING.md`.

**iOS: cero.** Nada se ha compilado ni probado.

---

## 3. Lo específico de iOS que cambió

### 3.1 Se quitó el target `iosX64()`

`composeApp/build.gradle.kts` ya no declara `iosX64()`. Motivo: Compose
Multiplatform 1.11.1, `androidx.lifecycle` 2.11.0 y `adaptive-navigation3`
1.3.0-beta02 **ya no publican artefactos para esa plataforma**, y Gradle escupía
cuatro bloques de error en cada compilación.

Se pierde el simulador de iOS en Macs **Intel**. Dispositivo real (`iosArm64`) y
simulador en Apple Silicon (`iosSimulatorArm64`) no se ven afectados.

> **Si el Mac es Intel, esto es un problema y hay que hablarlo con el usuario.**

### 3.2 `iOSApp.swift` y `InterstitialAdPreloader.kt`

El intersticial se desconectó del flujo mediante un interruptor en `commonMain`
(`AdConstants.INTERSTITIAL_ENABLED = false`). Swift no puede leer esa constante
directamente, así que `InterstitialAdPreloader.isInterstitialEnabled()` la expone y
`iOSApp.swift` solo precarga si está activa.

> **Revisa `composeApp/src/iosMain/.../ads/InterstitialAdPreloader.kt` con lupa.**
> Una limpieza automática de comentarios dejó `isInterstitialEnabled()` **dentro de
> un bloque KDoc**, y se arregló a mano. El equivalente en Android reventó la
> compilación; este no se ha compilado nunca.

### 3.3 Sin *launch screen* revisado

En Android se añadió un tema propio (`windowBackground` blanco) y un splash con
logo. **En iOS no se ha tocado nada**: el equivalente es el *launch screen* del
proyecto de Xcode. Anota qué se ve al arrancar.

---

## 4. Qué hay que verificar en el Mac

1. **Que Xcode compile y enlace el framework** tras quitar `iosX64()`.
2. **Persistencia de sesión en Keychain.** En Android se comprobó que la sesión
   sobrevive a una actualización; en iOS **nunca se ha ejercitado**, porque hasta
   esta rama la app no tenía auto-login. Es el criterio de aceptación 6.
3. Los bloques **2, 3 y 6** de `PRUEBAS-ONBOARDING.md`: arranque anónimo, vinculación
   de cuenta, y **aceptar y rechazar una lista compartida** (el del bug destructivo).
4. Qué se ve durante el arranque, para decidir si hace falta *launch screen*.

---

## 5. Configuración externa — estado actual

- **Proveedor anónimo**: habilitado en Firebase Console. Sin él, `signInAnonymously()`
  devuelve `ADMIN_ONLY_OPERATION`.
- **Reglas de Firestore**: en la **versión permisiva**. Se endurecieron, rompieron
  producción y se revirtieron.
  > **Orden correcto, aprendido a golpes:** publicar la app → esperar a que se
  > propague → **entonces** endurecer las reglas. Las reglas son del servidor y
  > afectan a todas las versiones instaladas al instante.
  > La versión endurecida está en la sección de reglas del plan.

---

## 6. Cómo se ha trabajado

- **No se ejecuta Gradle ni ningún comando de compilación.** El usuario compila a
  mano, siempre. Esto es una regla del proyecto, está en `CLAUDE.md`.
- **No se commitea ni se sube nada** sin que el usuario lo pida.
- El desarrollo lo hacen subagentes Sonnet fase a fase; la revisión del diff, línea
  a línea, la hace el agente principal.
- **Comentarios en el código: los mínimos.** El usuario pidió expresamente
  retirarlos. El porqué de cada decisión vive en el plan, no en el código.
- Hay un grafo del proyecto en `graphify-out/`; `graphify query "..."` para
  localizar código, y `graphify update .` tras modificarlo.

---

## 7. Trabajo aparte ya anotado (no tocar sin hablarlo)

Secciones 15, 18, 20 y 21 del plan: limpieza de cuentas anónimas huérfanas, avisos
push al compartir (FCM), sacar `interactions` de `composables/`, y llevar la
vinculación de cuenta a su propia ruta.

Y en el plan queda constancia de dos cosas que hoy **no funcionan y es a propósito**:

- **No se puede vincular con Google.** La librería `SignInKMP` hace
  `signInWithCredential()` por dentro y no expone el `idToken`, así que sustituye la
  sesión en vez de vincularla. Se implementó, se comprobó que era inalcanzable y se
  **borró**. Ver sección 3 del plan.
- **Compartir con alguien que no tiene la app no le avisa de nada.** Solo se escribe
  un documento en Firestore.
