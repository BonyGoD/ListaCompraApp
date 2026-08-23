# Pruebas — Alexa multiusuario

Guion de la sesión de pruebas, para pasar de una vez con las fases 1 a 5 desplegadas.
Referencia completa: `PLAN-ALEXA-MULTIUSUARIO.md`, sección 9.

**Pasada completa el 23 ago 2026.** Resultado abajo, en "Qué salió mal".

**Antes de empezar, marca esto:**

- [x] Fase 3 desplegada en producción (`api-devware`, rama `feature/alexa-resolver-usuario` mergeada)
- [x] Fase 5 compilada e instalada en Android
- [x] Fase 5 compilada e instalada en iOS
- [x] Reglas de Firestore publicadas, con TTL sobre `alexa_oauth_codes.expiraEn`
- [ ] Dos cuentas de prueba distintas, cada una con al menos una lista
- [x] Un Echo o la app de Alexa en el móvil
- [x] Consola de Firestore abierta

---

## Ya verificado — no hace falta repetirlo

Doce comprobaciones pasaron el 22 ago 2026 contra producción: el 401 sin firma, el
paso de una petición real de Alexa, el canje de código con PKCE, el replay
rechazado, el `invalid_client`, la rotación del refresh token y el
`alexaVinculada: true`. Están detalladas en la sección 9 del plan.

---

## Bloque 1 — Vinculación

| # | Paso | Esperado | OK |
|---|---|---|---|
| 1 | Hablar a la skill **sin estar vinculado** | Responde *"Para usar tu lista de la compra, vincula tu cuenta desde la app de Alexa"* **y la app de Alexa pinta el botón de vincular** | ✅ |
| 2 | Pulsar ese botón → entrar con **correo** | "Cuenta vinculada correctamente" | ✅ |
| 3 | Firestore | Hay doc en `alexa_refresh_tokens`; el de `alexa_oauth_codes` está `usado: true` | ✅ |
| 4 | Desvincular y repetir con **Google** | Igual que con correo | ✅ |
| 4b | Desvincular y repetir con **Apple** | Igual que con correo | ✅ |
| 5 | Intentar vincular en **sesión anónima** | Sale el aviso, no deja seguir | ⊘ |

> **Paso 1 — con matiz.** Se vincula, pero por Skill → Configuración → Vincular cuenta.
> La pantalla a la que lleva la **tarjeta `LinkAccount`** salía sin botón, y ese es el
> camino que usará la gente y el que prueba Amazon al certificar.
>
> Hubo una causa real y está arreglada: `Your Secret` estaba **vacío** en el Account
> Linking del console de Amazon, así que la configuración era inválida y por eso tampoco
> dejaba guardar. Se rellenó y guardó. Pero esta cuenta habilitó la skill mientras estaba
> rota y arrastra esa configuración; inhabilitar y volver a habilitar no la limpia, así
> que no se puede saber si lo que queda es residuo o un fallo de verdad.
>
> **Se decide en el paso 14**, con la segunda cuenta de Amazon, que nunca vio la
> configuración rota.

> **Pasos 4 y 4b — los tres proveedores vinculan en iOS, pero hubo un susto.** Correo,
> Google y Apple verificados el 23 ago 2026, todo en iPhone.
>
> Ese mismo día, vincular con Google devolvió una vez *"Unable to process request due to
> missing initial state ... storage-partitioned browser environment"*, y a los minutos, con
> el mismo código y la misma cuenta, vinculó bien. **No está arreglado: no ha vuelto a
> salir, que no es lo mismo.**
>
> Amazon abre la página en un navegador incrustado donde `window.open` no abre ventana:
> navega en la misma vista. El handler de `firebaseapp.com` deja su estado en el
> `sessionStorage` de **su** origen, y al volver del proveedor puede caer en otra partición
> y no encontrarlo. Que pase o no depende de si el navegador reutiliza la vista, y eso no
> se controla desde el código.
>
> **Lo grave no es el fallo, es que no tiene salida:** para entonces nuestra página ya no
> existe, así que el error no se puede capturar ni hay forma de devolver al usuario. Se
> queda en una pantalla en inglés sin botón de volver.
>
> Si reaparece durante la certificación, el arreglo de raíz es proxear `/__/auth/*` para
> que el handler sea del mismo origen, o vincular desde la app con **App-to-App account
> linking** y no pasar por ningún navegador.
>
> Reducir el riesgo antes de certificar cuesta poco: repetir Google y Apple cinco o seis
> veces seguidas, desvinculando entre medias, y anotar si falla alguna.

> **Paso 5 — no se puede probar por Alexa.** No hay forma de meter una sesión anónima en
> la app de Alexa: la vinculación empieza siempre desde una cuenta de Amazon. Lo que sí se
> prueba, y es lo que importa, es que la app **avise antes de dejar llegar hasta ahí** —
> eso es el paso 22. El rechazo del lado servidor (`access_denied` en
> `POST /alexa/oauth/code` cuando el idToken es anónimo) ya quedó verificado el 22 ago.

## Bloque 2 — Uso

| # | Paso | Esperado | OK |
|---|---|---|---|
| 6 | *"Alexa, abre compras pendientes"* | Bienvenida, ya no la tarjeta de vincular | ✅ |
| 7 | *"añade leche"* | Confirma y aparece en la app **sin recargar** | ✅ |
| 8 | Firestore | Está en `lista-compra/{listaAlexa}/productos` con `fecha` como Timestamp, `isImportant: false`, `isPurchased: false` | ✅ |
| 9 | *"añade papel de cocina"* | Entra completo, no truncado a "papel" | ✅ |
| 10 | *"ayuda"* | Responde algo útil, sesión abierta | ✅ |
| 11 | *"para"* y *"cancela"* | Cierran limpio | ✅ |

> Los pasos 10 y 11 son **obligatorios para certificar**: Amazon los prueba siempre.

> **`AMAZON.Food` sirve para lo que no es comida. Verificado el 23 ago 2026.** Entraron
> completos y sin deformarse: *tornillos*, *bombillas*, *papel de lija*, *pilas AA*,
> *ibuprofeno* y *arena del gato*.
>
> Se dio por bloqueante durante la sesión, y era falso: los slots predefinidos de Alexa
> funcionan como **datos de entrenamiento, no como lista cerrada**. Sesgan el
> reconocimiento hacia su dominio, pero no rechazan lo de fuera. La ficha puede seguir
> prometiendo ferretería y farmacia, y **no hace falta slot personalizado**.

## Bloque 3 — Colisión del nombre de invocación

**Superado por el propio cambio de nombre.** El bloque existía para decidir si había que
renombrar la skill; se renombró a **compras pendientes** antes de esta pasada, y esa
decisión ya está tomada. Los pasos 12 y 13 hablaban de *"lista de la compra"*, que ya no
es el nombre de nada.

| # | Frase | Resultado | OK |
|---|---|---|---|
| 12 | *"Alexa, añade leche a la lista de la compra"* | Va a la lista nativa de Amazon, **como se espera** | ⊘ |
| 13 | *"Alexa, pídele a lista de la compra que añada leche"* | Nombre obsoleto | ⊘ |

Que la frase del 12 se la quede la lista nativa **ya no es un fallo, es lo correcto**: es
justo lo que avisa el bloque de frases de la pantalla de Alexa en la app.

## Bloque 4 — Multiusuario

**Es la prueba que da nombre a todo esto. PENDIENTE: hace falta una segunda cuenta de Amazon.**

| # | Paso | Esperado | OK |
|---|---|---|---|
| 14 | Vincular la **segunda cuenta** con otra cuenta de Amazon, **entrando por la tarjeta `LinkAccount`** (ver nota del paso 1) | Vincula, y esta vez la pantalla **sí** trae botón | ☐ |
| 15 | Añadir un producto desde cada una | Ambas confirman | ☐ |
| 16 | Firestore y las dos apps | **Cada producto en su lista. Ninguna cuenta ve el de la otra** | ☐ |

> **Que desde una cuenta no aparezcan productos ajenos no sustituye a esta prueba.** Lo
> que demuestra es que se respeta el `uid` del token al escribir, y eso ya se sabía. Lo que
> falta por ver es que **dos cuentas de Amazon distintas produzcan `uid` distintos**, y eso
> solo se ve teniendo las dos. El riesgo es bajo —el `uid` sale del JWT y no hay estado
> compartido en medio— pero bajo no es cero, y es el titular de todo el plan.

## Bloque 5 — Lista y caducidad

| # | Paso | Esperado | OK |
|---|---|---|---|
| 17 | Menú lateral → **Alexa** → elegir una segunda lista | Se marca como activa | ✅ |
| 18 | *"añade huevos"* | Va a la **nueva** lista | ✅ |
| 19 | Quitar `listaAlexa` a mano en Firestore y dictar otro producto | Cae en `listas[0]`, la predeterminada | ✅ |
| 20 | Esperar a que caduque el access token (>1 h) y volver a dictar | Funciona sin intervención: Alexa refresca sola | ✅ |

> El paso 17 ya no se llega desde MisListas: la sección se mudó a pantalla propia, en el
> menú lateral.

## Bloque 6 — La app, en los dos sistemas

| # | Paso | Esperado | OK |
|---|---|---|---|
| 21 | **Android**: pantalla de Alexa con cuenta vinculada | Selector visible, lista activa marcada | ✅ |
| 22 | **Android**: la misma pantalla en sesión anónima | Aviso, sin selector, y el botón lleva a crear cuenta | ⚠️ |
| 23 | **Android**: cuenta sin vincular | Explica que se vincula desde la app de Alexa | ✅ |
| 24 | **Android**: usuario **sin ninguna lista** | Se ve la pantalla de Alexa **y** en MisListas el mensaje de "no tienes listas" | ✅ |
| 25 | **iOS**: repetir 21, 17 y 18 | Igual que en Android | ✅ |
| 26 | Los tres idiomas: español, catalán e inglés | Ningún texto sin traducir ni mal escrito | ✅ |

---

## Qué salió mal — 23 ago 2026

Seis cosas. Cuatro son de texto y presentación; la cuarta y la sexta son bugs de verdad.

### 1. El saludo de la skill nombraba a la competencia

`ALEXA_RESPONSES.WELCOME` decía *"Abriendo **lista de la compra**. ¿Qué quieres añadir?"*.
Dices *"abre compras pendientes"* y te contesta con el nombre de otra cosa — y no de
cualquier cosa: **"lista de la compra" es el nombre de la lista nativa de Alexa**, la
confusión que lleva todo el plan intentando evitar.

→ `api-devware`, `api/constantes.js`.

### 2. El aviso de sesión anónima, mal redactado

Decía *"necesitas una cuenta con correo o Google"*, que describe el requisito por sus
proveedores en vez de decir qué tiene que hacer el usuario. Y la acción era un texto
pulsable, no un botón.

→ `mislistas_alexa_anonymous_message` y `mislistas_alexa_link_account_button`, tres idiomas,
más el `TextButton` de `AlexaSection.kt`.

**El destino del botón NO cambia**, aunque diga "Crear cuenta": sigue abriendo el diálogo
de vinculación. Decisión tomada con las tres opciones sobre la mesa el 23 ago 2026, porque
`UsersDataSource.userRegister()` llama a `createUserWithEmailAndPassword` y devuelve
`listas = listOf()` — o sea, `Routes.Register` crea una cuenta nueva y **abandona las listas
del anónimo**, mientras que `linkAccountWithEmailUseCase` las conserva. Por eso el menú
lateral ya enseña un `DataLossWarningDialog` antes de mandar a Login.

### 3. El camino dentro de la app de Alexa estaba mal descrito

`mislistas_alexa_not_linked_message` decía *"Más → Skills, busca la skill y pulsa Vincular
cuenta"*. El circuito real, comprobado:

| Situación | Camino |
|---|---|
| Skill ya instalada | Skills y juegos → Mis skills → Activado → la skill → Configuración → Vincular cuenta |
| Sin instalar | Skills y juegos → buscarla → instalar → Configuración → Vincular cuenta |

### 4. El diálogo de crear cuenta reaparecía solo — **bug**

Entrar en la pantalla de Alexa y volver atrás hacía salir el diálogo de "Crea tu cuenta"
sin pedirlo. En Android y en iOS.

`AlexaViewModel` navegaba con `Routes.Home(userId, openLinkAccount = true)`, y ese flag se
quedaba grabado **para siempre** en esa entrada del backstack. `HomeScreen` lo lee en un
`LaunchedEffect(Unit)`, y `NavDisplay` descompone Home al ir a Alexa y la recompone al
volver: el efecto se relanzaba, veía el flag todavía a `true` y reabría el diálogo. Cada vez.

**El primer arreglo fue peor que el bug.** Un guard con `rememberSaveable` en `HomeScreen`
dio la vuelta al problema: como `Routes.Home` es un data class, volver a pedir la
vinculación produce una entrada **igual** a la anterior, `NavDisplay` le restaura su estado
guardado, el guard seguía consumido y el diálogo ya no salía **nunca más** en cuanto lo
cancelabas una vez.

Los dos bugs son el mismo error de fondo: **una ruta no es un evento**. Un parámetro de
navegación describe *dónde estás*, y aquí se estaba usando para decir *qué acaba de pasar*.

→ `PendingHomeAction`, un objeto de Koin con una petición de un solo uso:
`requestLinkAccount()` la deja puesta, `consumeLinkAccount()` la devuelve una vez y la apaga.
El flag sale de `Routes.Home` por completo, así que la clase entera de bug desaparece.

### 5. Apóstrofes escapados a la vista, en catalán e inglés

Se leía `S\'ha produït un error`. `composeResources` **no procesa el escape `\'`** de
Android XML: lo pinta tal cual. En español no se notaba porque no lleva apóstrofes.

Seis casos en `values-ca/strings.xml`, dos en `values/strings.xml`. En ese mismo fichero
catalán ya había apóstrofes sin escapar que se veían bien — esa es la forma correcta.

### 6. Al crear la cuenta desde Alexa saltaba el diálogo de compartir — **bug**

Creas la cuenta desde la pantalla de Alexa y, nada más terminar, se abre el diálogo de
compartir lista. Nadie lo ha pedido.

`linkAccountWithEmail()` terminaba siempre en `setState { showCustomDialog(true) }`, y ese
`customDialog` **es** el `ShareListaCompraDialog`. Estaba bien mientras el único camino para
llegar a vincular fuera "compartir requiere cuenta", donde abrir el diálogo de compartir al
acabar es exactamente lo que quieres. La pantalla de Alexa añadió un segundo camino y heredó
el final del primero. Lo mismo pasaba en `signInWithExistingAccountAndDiscardAnonymous()`.

→ `LinkAccountOrigin { SHARE, ALEXA }` en `ListaCompraState`, se fija al abrir el diálogo y
se consulta al cerrarlo. El valor por defecto es `SHARE`, que es el comportamiento de antes:
si algún día aparece un tercer camino y se olvida marcarlo, se comporta como siempre en vez
de callarse.

---

## Qué queda por probar

1. **Bloque 4 entero.** Necesita una segunda cuenta de Amazon. Es la prueba que da nombre al plan.
2. **El paso 1 por la tarjeta `LinkAccount`**, con esa misma cuenta limpia. Si sigue sin botón, bloquea la certificación.
3. **Crear cuenta desde la pantalla de Alexa, en Android y en iOS**, con el arreglo del punto 6: debe terminar sin diálogo de compartir y **con las listas del anónimo intactas**.
4. **Cancelar ese diálogo y volver a pedirlo.** Tiene que salir otra vez, tantas como haga falta. Es lo que rompió el primer arreglo del punto 4.
5. **Volver atrás desde la pantalla de Alexa sin haber pulsado nada.** No debe salir ningún diálogo. Es el bug original.
6. **Compartir una lista siendo anónimo**, que es el camino que NO debía cambiar: ahí el diálogo de compartir sí tiene que salir al crear la cuenta.

Los pasos 23 y 26 quedaron verificados el 23 ago 2026 con los arreglos ya puestos.

---

## Si algo falla

- **La skill responde 401 a todo** → mira los logs de Vercel. Si dice `ALEXA_SKILL_ID no
  está configurada`, falta la variable en ese entorno. Si dice `ALEXA_JWT_SECRET no está
  configurada`, lo mismo. Cualquier otra cosa es fallo de firma real.
- **La app de Alexa no pinta el botón de vincular** → antes de tocar código, mira
  `Your Secret` en el Account Linking del console. Si el campo enseña el placeholder
  "Enter client secret", está **vacío**, la configuración es inválida y por eso tampoco
  te deja guardar. Pasó el 23 ago 2026.
- **Vincula pero la skill sigue pidiendo vincular** → el access token no verifica. Casi
  seguro que `ALEXA_JWT_SECRET` no es la misma con la que se firmó, o falta.
- **Vincula y dicta bien pero no aparece en la app** → mira a qué `listaId` fue en los logs
  (`Producto "X" guardado en lista Y`) y compáralo con `usuarios/{uid}.listaAlexa` y
  `listas[0]`.
- **Todo va bien pero llega tarde** → el endpoint de token tiene 4,5 s de límite y el de la
  skill 8 s. Medidos: 1,85 s en tibio y 0,47 s en caliente. En frío no está medido.
