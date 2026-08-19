# Pruebas — rama `feature/onboarding-sin-registro`

Marca cada paso: `[x]` OK · `[!]` KO · `[ ]` sin probar.
Si algo sale KO, anótalo en la línea y sigue con el resto del bloque.

---

## Preparación

- [X] Reglas de Firestore en la **versión permisiva** (rollback hecho)
- [X] Proveedor **anónimo** habilitado en Firebase Console
- [X] **Borrado** de `notifications` el documento con `email` vacío
- [X] **Cuenta A** lista: real, con **dos listas propias** y productos distintos
- [X] **Correo B** y **correo C** disponibles, sin cuenta

---

## Bloque 1 — Compila

- [X] 1. Compila debug sin errores
- [X] 2. **Ya no salen los cuatro errores de `iosX64`**
      *(si siguen: invalidar caché de configuración de Gradle antes de sospechar)*

## Bloque 2 — Arranque e instalación limpia

- [X] 3. Desinstalar e instalar → entra **directo a una lista vacía**, sin login ni anuncio
- [X] 4. Se ve el **logo sobre fondo blanco** mientras carga
- [X] 5. **No aparece ninguna notificación**
- [X] 6. Añadir dos productos → cerrar del todo → reabrir → **siguen ahí**

## Bloque 3 — Vincular con un correo nuevo

- [X] 7. Anotar el **UID** del anónimo en Authentication → `yb8tkk5gFaUS1dswcU3DiLdkTm92`
- [X] 8. Menú lateral → "Compartir lista" → sale *"Compartir necesita una cuenta"*
- [X] 9. "Crear cuenta" → el botón pone **"Continuar" en una sola línea**
- [X] 10. El texto dice que la lista **se trasladará** a la cuenta nueva
- [X] 11. Correo **B** + contraseña → Continuar
- [X] 12. **Se abre solo el diálogo de compartir**, con los productos intactos
- [X] 13. En Authentication: **el mismo UID**, ahora con correo B y sin marca de anónimo

## Bloque 4 — Validación al compartir

- [x] 14. Compartir con el **campo vacío** → error, y **no** se crea documento
- [x] 15. Compartir con `hola` → mismo error
- [x] 16. Compartir con `  Correo@Ejemplo.com ` → en Firestore queda **en minúsculas y sin espacios**

> El 16 valida además que el reordenamiento de parámetros no rompió el compartir.

## Bloque 5 — Recibir una invitación sin reiniciar

- [X] 17. Sin cerrar la app, crear a mano en Firestore un documento en `notifications`
      con `email` = correo B, `nombre` = "Prueba", `listaId` = cualquiera
- [!] 18. **La notificación aparece en la app sin reiniciar** - Solo aparece al reiniciar.
      → **ARREGLADO, repetir.** Faltaba rehacer la suscripción al vincular.

## Bloque 6 — El crítico: aceptar y rechazar

- [X] 19. Cuenta **A** con sus dos listas propias y productos confirmados
- [X] 20. Compartir una lista con el correo de A
- [X] 21. **Rechazar** la invitación → **no crashea**
- [X] 22. Compartir otra vez y **aceptar** → **no crashea**
- [X] 23. **A se queda con TRES listas**, las dos suyas con sus productos intactos
- [X] 24. La lista compartida se ve **con su nombre real**
- [X] 25. En Firestore: **no ha desaparecido ningún documento** de `lista-compra` - No ha desaparecido ningun documento pero en el map nombresListas solo aparecen las 2 que tenia, no la compartida
      → **ARREGLADO, repetir en el bloque 10.**

> Los pasos 21 y 22 son los del crash. Los dos iban por el mismo camino roto.

## Bloque 7 — Regresiones

- [X] 26. Login con la cuenta A **por correo**
- [X] 27. Login **con Google**
- [X] 28. **Registro** de una cuenta nueva
- [X] 29. **Borrar una cuenta** de prueba *(se tocó sin pedirlo)*
- [X] 30. **En ninguno aparece el intersticial**
- [X] 31. Provocar un error (compartir sin red) → **la alerta sale con su título**

## Bloque 8 — Idiomas

- [X] 32. Móvil en **inglés** → menú lateral **entero** en inglés, sin mezcla
- [X] 33. Móvil en **catalán** → ídem
- [X] 34. Una lista sin nombre se llama **"Lista de la compra"**, no "Lista 1"
El boton Agregar producto y el Borrar lista no estan traducidos.

## Bloque 8 bis — Repesca tras los arreglos

- [X] 34a. **Repetir el paso 18**: vincular cuenta y, sin reiniciar, crear a mano la
      invitación en Firestore → **debe aparecer sola**
- [X] 34b. Tras vincular, comprobar que **la lista y sus productos siguen bien**
      (se añadió una recarga de datos en ese camino)
- [X] 34c. Con una cuenta **que no sea la del desarrollador**: los botones
      **"Forzar crash (test)" y "Forzar non-fatal (test)" NO aparecen**
- [X] 34d. Con **bonygod.dev@gmail.com**: **sí aparecen** y siguen funcionando
- [X] 34e. Móvil en inglés → **"Add product"**, **"Clear list"** y el diálogo de
      confirmación de borrado, traducidos

## Bloque 9 — iOS (en el Mac)

- [X] 35. **Xcode compila y enlaza el framework** tras quitar `iosX64()`
- [!] 36. Repetir bloque 2 (arranque) — el arranque va bien, pero el **banner tarda
      10-15 s** en aparecer y mientras se ve **el hueco en negro**, tanto al arrancar
      como al ir y volver entre pantallas.
      → **ARREGLADO, repetir.** Contenedor con color de fondo y reutilizado entre
      navegaciones. Ver sección 23 del plan.
- [!] 37. Repetir bloque 3 (vincular) — la vinculación funciona, pero en **iPhone X**
      el botón **"Crear cuenta" parte en dos líneas**.
      → **ARREGLADO, repetir.** `maxLines = 1` y menos padding interno.
- [X] 38. Repetir bloque 6 (aceptar y rechazar)
- [!] 39. Anotar qué se ve al arrancar → **pantalla negra**, y después el splash con
      logo y spinner. `UILaunchScreen` estaba vacío en `Info.plist`, así que iOS usaba
      `systemBackground`: negro con el móvil en modo oscuro.
      → **ARREGLADO, repetir.** Fondo blanco fijo. Ver sección 23.3 del plan.
- [X] Extra. **Persistencia de sesión en Keychain** (criterio 6): entra directo sin
      teclear contraseña tras actualizar

## Bloque 9 bis — Repesca de iOS

- [X] 40. Arrancar: **donde va el banner se ve el color de fondo de la app, no negro**
- [X] 41. Ir a otra pantalla y volver a la lista: **el banner ya está, sin espera**
- [X] 42. Con el móvil en **inglés** y en **catalán**, el diálogo "Compartir necesita
      una cuenta": **"Create account" / "Crear compte" en una sola línea y sin cortar**
- [X] 43. **Con el móvil en modo oscuro**: al arrancar se ve **blanco**, no negro, y
      enlaza con el splash sin parpadeo
- [X] 44. Repetir en modo claro

## Bloque 10 — El nombre de la lista compartida

Probado en **iOS** el 19 ago 2026, tras el tercer intento: el nombre viaja ahora en
la invitación (`notifications.listaNombre`), resuelto por quien comparte.

- [X] 45. Aceptar una invitación → en Firestore, `usuarios/{uid}.nombresListas`
      contiene **también la lista compartida**, con su nombre real
- [X] 46. La lista compartida se ve **con su nombre**, no "Lista de la compra"
- [-] 47. Aceptar **dos veces** la misma invitación — **no alcanzable**:
      `deleteNotification()` borra por `(email, listaId)`, así que al aceptar
      desaparecen todas las invitaciones de esa lista. La guarda contra duplicados
      sigue en el código (`currentListas.contains(listaId)` y sobrescritura de la
      clave del mapa), pero por la interfaz no hay forma de dispararla.
      → Variante que **sí** se puede probar si algún día interesa: compartir la
      misma lista **otra vez, ya aceptada**, y volver a aceptarla.
- [X] 48. Renombrar una lista propia sigue funcionando *(toca el mismo mapa)*

### Bloque 10 bis — Pendiente en Android

El bloque 10 se validó solo en iOS. El código es de `commonMain`, pero el resto de
la rama se probó primero en Android, así que falta cerrar el círculo.

- [X] 49. Repetir **45, 46 y 48 en Android**, compartiendo de nuevo *(las
      invitaciones anteriores al cambio no llevan `listaNombre`)*
      → **HECHO** el 20 ago 2026, en el bloque 12.2.

## Bloque 11 — Reglas endurecidas de `notifications`

Publicadas y probadas en **iOS** el 19 ago 2026, con la versión estricta — la que
normaliza con `.lower().trim()` en la propia regla.

- [X] 50. Compartir → **la invitación aparece sola**, sin reiniciar
- [X] 51. Aceptarla → **desaparece de la lista** *(el borrado pasa por el mismo
      `allow read, delete`, y hace otra query antes)*
- [X] 52. Con sesión **anónima**: no se ve ninguna notificación *(criterio 9, ahora
      impuesto por el servidor: un anónimo no tiene `email` en el token)*
- [X] 53. Nada de esto se rompió en `lista-compra` ni en `productos`: sus reglas no
      cambiaron entre las dos versiones

> No se repiten en Android: las reglas son del servidor y la query sale del mismo
> código de `commonMain`, así que no hay comportamiento específico de plataforma
> que probar.

---

## Bloque 12 — Android tras el cierre de iOS

Probado el 20 ago 2026, con las **reglas estrictas ya en producción** y sobre el
código de `8f1b185`. Cierra los dos huecos de la tanda anterior de Android: se
había probado con las reglas permisivas y antes del último commit de `commonMain`.

### 12.1 Aceptar y rechazar, ahora con las reglas estrictas

Los pasos 21-22 se probaron con las permisivas. El crash de la sección 16 del plan
solo aparecía con las estrictas, así que esto no es una repetición: es el camino
que se rompió, nunca ejercitado en Android bajo las reglas de hoy.

- [X] 54. Desde **B**, compartir una lista con el correo de **A**
- [X] 55. En **A**, la invitación **aparece sola**, sin reiniciar
- [X] 56. **Rechazar** → desaparece de la hoja y **no crashea**
- [X] 57. Compartir otra vez y **aceptar** → **no crashea**, y la invitación desaparece
- [X] 58. **A se queda con TRES listas**, las dos suyas con sus productos intactos
- [X] 59. Con sesión **anónima** (instalación limpia): **no se ve ninguna notificación**

### 12.2 El nombre de la lista compartida — cierra el ítem 49

- [X] 60. Tras aceptar, `usuarios/{uid}.nombresListas` contiene **también la lista
      compartida**, con su nombre real
- [X] 61. En Mis Listas, la compartida se ve **con su nombre**, no "Lista de la compra"
- [X] 62. **Renombrar una lista propia** sigue funcionando *(toca el mismo mapa)*

### 12.3 Las 14 cadenas nuevas

Antes estaban en duro; `8f1b185` las pasó a `stringResource`. Estaban validadas
solo en iOS.

- [X] 63. En **inglés**, hoja de Notificaciones: "Notifications", "… shared their
      list with you", "Accept", "Cancel"
- [X] 64. En **catalán**: "Notificacions", "… t'ha compartit la seva llista",
      "Acceptar", "Cancel·lar"
- [X] 65. En **inglés**, Mis Listas: "My lists" y el badge "Default"
- [X] 66. En **inglés**, diálogo de lista nueva: "New list", "Name", "Create", "Cancel"
- [X] 67. En **inglés**, diálogo de renombrar: "Rename list" / "Rename"
- [X] 68. Repetido en **catalán**: "Les meves llistes", "Predeterminada", "Llista
      nova", "Nom", "Crear", "Cancel·lar", "Reanomenar llista", "Reanomenar"

> `mislistas_empty_message` no es alcanzable por interfaz: siempre hay una lista
> predeterminada. Sin probar a propósito.

### 12.4 Regresión del diálogo "Compartir necesita una cuenta"

Es el paso 9, pero el botón cambió después: los **dos** botones llevan ahora
`contentPadding = 8.dp` y `maxLines = 1`, ajustados para un iPhone X.

- [X] 69. Con sesión anónima → menú lateral → Compartir lista → sale el diálogo
- [X] 70. **"Cancelar" y "Continuar" en una línea cada uno, sin recortar**, en
      **es**, **en** y **ca**

### 12.5 Arranque, por ser APK nuevo

- [X] 71. Desinstalar e instalar → entra **directo a una lista vacía**, sin login ni
      anuncio, con el logo sobre fondo blanco
- [X] 72. Añadir dos productos → cerrar del todo → reabrir → **siguen ahí**

### 12.6 Humo sobre el build de release

Todo lo anterior se probó en debug. El release lleva `isMinifyEnabled` e
`isShrinkResources` activados, y los caminos nuevos —sesión anónima, splash,
`linkWithCredential`, `listaNombre` en la invitación— no habían corrido nunca bajo
R8. **No hizo falta ninguna regla nueva**: los modelos `@Serializable` ya caen bajo
el `-keep` de `**.data.model.**`, Koin registra por constructor y no por reflexión,
y Firebase Auth está protegido entero.

- [X] 73. Instalación limpia del **release firmado** → entra directo a la lista
      vacía, con sesión anónima
- [X] 74. Compartir → vincular cuenta con correo → se abre el diálogo de compartir
- [X] 75. Aceptar una invitación → la lista aparece **con su nombre**

---

## Pendiente antes de publicar

- [X] ~~Endurecer las reglas de `notifications`~~ — hecho y probado (bloque 11)
- [X] ~~Bloque 10 bis: `nombresListas` en **Android**~~ — hecho en el bloque 12.2
      (20 ago 2026)
- [X] ~~Probar en **inglés y catalán** la hoja de notificaciones y Mis Listas~~ —
      las 14 cadenas nuevas, correctas en los dos idiomas (iOS 19 ago, Android 20 ago 2026)
- [X] ~~**Subir la versión** de iOS~~ — `MARKETING_VERSION` 1.3.0 y
      `CURRENT_PROJECT_VERSION` 15
- [X] ~~**Subir la versión** de Android~~ — `versionCode` 23 y `versionName` 1.3.0
- [X] ~~**Humo sobre el release firmado**~~ — ver 12.6
- [ ] Publicar

> **Urgencia, no es una preferencia.** Las reglas estrictas se publicaron el 19 ago
> y están vivas; en Play sigue **1.2.0**, cuyo `deleteNotification()` lleva el
> `where { }` que descarta el filtro de correo y cuyo repositorio hacía `throw` en
> vez de devolver `Result`. Cualquier usuario en 1.2.0 que acepte o rechace una
> invitación **crashea ahora mismo**. Es la sección 16 del plan repetida. Si la
> publicación se va a demorar días, devolver mientras el bloque de `notifications`
> a `allow read, write, delete: if request.auth != null;`.

## Si algo falla

Logcat filtrado por el paquete, y **Crashlytics** para las excepciones: los
repositorios registran todas con `recordException` antes de devolver el fallo.
Los mensajes en pantalla son genéricos a propósito.
