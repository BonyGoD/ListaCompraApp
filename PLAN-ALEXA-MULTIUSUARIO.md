# Plan — Alexa para todos los usuarios (account linking)

**Ramas:** `feature/alexa-account-linking`, creada desde `develop` el 22 ago 2026 en los dos repos:

| Repo | Local | Base |
|---|---|---|
| `BonyGoD/ListaCompraApp` (app KMP) | `C:\Users\bony1\projects\lista-compra-app` | `b22e0ee` |
| `BonyGoD/api-devware` (API Express en Vercel) | `C:\Users\bony1\projects\api-devware` | `9b4e25c` |

**Estado a 22 ago 2026:** **Fase 1 terminada, desplegada en producción y verificada.**
`api-devware` v1.1.0 en `main`. `POST /alexa` sin firma devuelve 401 (comprobado también
con un envelope bien formado y el `applicationId` correcto: sigue rechazando, luego el corte
lo hace la firma). El resto de rutas intactas: `/fotos` responde 200 y el formulario de
`devware.es` envía correo. Siguiente: fase 2 (servidor OAuth2).

**22 ago 2026 — el flujo completo funciona en producción.** Vinculación real desde la app de
Alexa con correo, producto dictado y escrito en la lista del usuario (la predeterminada, vía
el respaldo `listas[0]`, porque `listaAlexa` aún no existe hasta que se publique la fase 5).
Fases 1, 2 y 3 validadas juntas. API en 1.2.0.

**Fases 1 a 5 verificadas.** Vinculación real desde la app de Alexa con **correo y con
Google** — `signInWithPopup` aguanta dentro del webview de Amazon, que era la duda abierta
desde que se escribió la fase 2. **Multiusuario probado con dos móviles**: cada cuenta
escribe en su lista y ninguna ve la de la otra (criterio nº7). Fase 5 compilada y
funcionando.

**Pendiente, todo en el console de Amazon:**
1. **Renombrar la invocación.** La colisión está confirmada: *"añade leche a la lista de la
   compra"* se lo queda la lista nativa de Alexa y no se recupera. Acordado: `lista del súper`.
2. **Ampliar las sample utterances.** *"Pídele a X que **añada** leche"* tampoco funciona, y
   la causa probable no es el nombre sino que el modelo solo cubre el indicativo: la
   construcción one-shot necesita que la parte final case con una utterance, y `añada`,
   `apunte`, `meta`, `agregue`, `ponga` no están.
3. Volver a probar las frases 12 y 13 con el nombre nuevo y las utterances puestas.
4. Certificar (6.6 a 6.8).

**Fase 0 — hecho:** diagnóstico (0.3), URL de la API, app Web de Firebase (ya existía),
Skill ID, redirect URIs, nombre de invocación, Authorized domains.

Las cinco `ALEXA_*` están creadas en Vercel (`CLIENT_SECRET` y `JWT_SECRET` como Sensitive).
**La fase 1 está desbloqueada.**

**Fase 0 — pendiente, en consolas y de BonyGoD:**
1. Prueba de colisión del nombre de invocación (6.2 bis) → no bloquea la fase 1; hacerla pronto.
2. Rotar `FIREBASE_PRIVATE_KEY` (0.7) → no bloquea nada, pero cuanto antes mejor.
3. Rellenar Account Linking en el console de Alexa con las URIs y el client id/secret
   (fase 6.5) → hace falta **antes de probar** la fase 2, no antes de escribirla.

---

## 0. Estado actual, verificado leyendo el código

### 0.1 Lo que hay hoy en la API

`api/models/alexaModel.js` escribe así:

```js
db.collection("lista-compra").add({ producto, fecha: new Date() })
```

Es decir: crea un **documento suelto en la raíz de `lista-compra`** con los campos
`producto` y `fecha`. No hay usuario, no hay lista, no hay nada que identifique a nadie.
Eso es exactamente el "solo funciona para mí": no es que esté atado a tu cuenta, es que
**no está atado a ninguna** — escribe siempre en el mismo sitio.

### 0.2 Lo que espera la app

`ListaCompraDataSource.getProductos(listaId)` lee de:

```
lista-compra/{listaId}/productos/{productoId}
```

con los campos `producto`, `fecha` (Timestamp), `isImportant`, `isPurchased`.

### 0.3 Desajuste de rutas — **confirmado el 22 ago 2026**

**Las dos rutas no coinciden.** La API escribe en `lista-compra/{idAuto}` (un documento
de primer nivel) y la app lee de `lista-compra/{listaId}/productos/*` (una subcolección).
Un documento escrito por la API nunca aparecería en la app.

**Causa, confirmada por BonyGoD:** cuando la app era de un solo usuario, los productos
colgaban directamente de `lista-compra` y la API encajaba. Al abrirla a varios usuarios y
varias listas se introdujo el nivel `{listaId}/productos`, **y la API se quedó atrás**.
No es que Alexa esté atada a su cuenta: es que escribe en un modelo de datos que ya no existe.

**Consecuencia:** en la raíz de `lista-compra` hay documentos huérfanos con campo `producto`
— los que Alexa fue dejando ahí después de la migración y que la app nunca llegó a mostrar.
**Se dejan como están** (decisión de BonyGoD, 22 ago 2026): no los lee nadie y borrar en
producción no compensa. Si algún día aparecen en una auditoría de Firestore, esto es lo que son.

### 0.4 Lo que falta para poder publicar la skill

Tres cosas, y las tres son **bloqueantes para certificar**:

1. **No hay verificación de firma.** `alexaRoute.js` acepta cualquier POST. Amazon exige
   validar las cabeceras `Signature-256` y `SignatureCertChainUrl` en cada petición. Sin
   esto la skill **no pasa certificación**, y mientras tanto cualquiera que conozca la URL
   puede escribir en tu Firestore.
2. **No se valida el `applicationId`.** Otra skill podría apuntar a tu endpoint.
3. **No hay account linking.** No existe forma de saber qué usuario habla.

### 0.5 Código muerto que hay en `alexaModel.js`

`getProducts()` y `deleteProduct()` no los llama nadie (`AlexaController` solo usa
`addProduct`). O se adaptan al nuevo modelo o se borran. **Decisión: se borran** —
si luego hace falta un intent de "¿qué tengo en la lista?" se escribe con el modelo bueno.

### 0.6 Detalle menor pero anotado

`usuarios/{uid}` ya tiene un campo `apiKey` que `createUserDocument` siempre escribe como
`""` y que nadie lee nunca. Estaba puesto pensando en algo así. **No lo reutilizamos**
(un token de Alexa no es una API key del usuario), pero tampoco lo tocamos en esta rama.

### 0.7 `FIREBASE_PRIVATE_KEY` hay que rotarla (independiente de este plan)

En Vercel, `FIREBASE_PRIVATE_KEY` y `FIREBASE_API_KEY` salen marcadas **"Needs Attention"**.
Las dos cosas no pesan lo mismo:

- **`FIREBASE_API_KEY` da igual.** Es la clave web: un identificador público que viaja en el
  JavaScript de cualquier página con Firebase. No protege nada; lo que protege son las reglas
  de Firestore y Auth. Se puede marcar Sensitive para quitar el aviso, pero no urge.
- **`FIREBASE_PRIVATE_KEY` sí importa, y bastante.** Es la clave privada de la cuenta de
  servicio: da **acceso total a todo Firestore saltándose las reglas de seguridad**. Quien
  la tenga puede leer y borrar los datos de todos tus usuarios.

En el [incidente de seguridad de Vercel de abril de 2026](https://vercel.com/kb/bulletin/vercel-april-2026-security-incident),
un atacante llegó a **enumerar y descifrar variables de entorno no marcadas como Sensitive**
— justo la categoría en la que está esta. Vercel recomienda rotar las credenciales afectadas.

**Qué hacer** (10 min, y no depende de este plan — se puede hacer hoy):
1. Firebase Console → ⚙ → Cuentas de servicio → **Generar nueva clave privada**.
2. En Vercel, borrar `FIREBASE_PRIVATE_KEY` y volver a crearla con el valor nuevo, esta vez
   con el interruptor **Sensitive** activado (no se puede convertir en el sitio: hay que
   borrar y recrear).
3. Redesplegar y comprobar que la API sigue arrancando (`✅ Firebase Admin SDK inicializado`).
4. Firebase Console → revocar la clave antigua.

De las cinco variables nuevas de este plan, `ALEXA_CLIENT_SECRET` y `ALEXA_JWT_SECRET` se
crean **Sensitive desde el principio**.

---

## 1. Objetivo

Que cualquier usuario de la app pueda decirle a Alexa *"añade leche a la lista de la
compra"* y el producto aparezca **en su lista**, con la skill publicada en la Alexa
Skill Store en español.

---

## 2. Decisión técnica: OAuth2 propio, no código de emparejamiento

Las opciones reales eran dos:

**Código de emparejamiento** — la app genera un código de 6 dígitos, el usuario se lo dicta
a Alexa. **Descartada**: dictar códigos a un asistente de voz falla constantemente, y las
guías de certificación de Amazon lo desaconsejan explícitamente. Te arriesgas a un rechazo.

**Account linking con authorization code grant** — la elegida. Es lo que Amazon recomienda,
es lo que la gente espera (se vincula desde la app de Alexa, como cualquier otra skill), y
tiene una ventaja que decide el asunto: **el token viaja en cada petición**
(`context.System.user.accessToken`), así que el endpoint sabe quién habla sin estado propio.

El coste es montar un servidor OAuth2. Suena peor de lo que es: son dos endpoints y una
página HTML de login, y como la API ya tiene Firebase Admin, **verificar la identidad es
una llamada** (`admin.auth().verifyIdToken()`). Amazon publica una implementación de
referencia en [alexa-samples/alexa-oauth-sample](https://github.com/alexa-samples/alexa-oauth-sample).

Requisitos de Amazon que condicionan el diseño:

- HTTPS en el puerto 443 con certificado de una CA reconocida → Vercel ya lo cumple.
- **PKCE con `S256`** (es el único método de challenge soportado).
- El access token debe durar **como mínimo 360 segundos**.
- El endpoint de token debe **responder en menos de 4,5 segundos**.
- Hay que devolver también un refresh token.

---

## 3. Modelo de datos

### 3.1 Cambios en `usuarios/{uid}`

| Campo | Tipo | Quién lo escribe | Para qué |
|---|---|---|---|
| `listaAlexa` | `string` | La app | Id de la lista donde escribe Alexa. Si falta, se usa `listas[0]`. |
| `alexaVinculada` | `boolean` | La API (Admin SDK) | Para que la app muestre el estado. **Puede dar falsos positivos**, ver abajo. |
| `alexaVinculadaEn` | `Timestamp` | La API (Admin SDK) | Informativo. |

### 3.2 Colecciones nuevas (solo Admin SDK, ningún cliente las toca)

**`alexa_oauth_codes/{code}`** — códigos de autorización de un solo uso.

```
uid            string
clientId       string
redirectUri    string
codeChallenge  string      // PKCE, base64url del SHA-256
expiraEn       Timestamp   // ahora + 10 min
usado          boolean
```

> **Por qué `alexaVinculada` daba falsos positivos (detectado el 22 ago 2026).** En el diseño
> original lo escribía el endpoint de token al emitir las credenciales. Pero emitir un token
> **no** significa que Amazon lo haya guardado: el flujo puede fallar después, en el
> `accountLink/establish` del lado de Amazon, y la API nunca se entera. Pasó en la primera
> prueba manual y dejó el campo en `true` sin vinculación real, con la app enseñando el
> selector y la skill pidiendo vincular a la vez.
> **Arreglo (22 ago), y su reversión (23 ago).** Primero se movió la escritura al endpoint
> de la skill, para que solo la marcara un `accessToken` realmente recibido. **Fue un error
> y se ha revertido.** El falso positivo solo puede darse lanzando el flujo a mano con un
> `state` que Amazon no ha emitido: cuando el usuario pulsa "Vincular" en la app de Alexa es
> Amazon quien lo inicia y quien lo completa. A cambio, aquel cambio provocaba que la app
> dijera **"Alexa · Sin vincular" justo después de vincular con éxito**, hasta que el usuario
> dictara un producto. Se cambió un fallo imposible por uno que sufría todo el mundo.
>
> **Estado definitivo:** lo marca el endpoint de token al emitir las credenciales, y
> `AlexaModel.addProduct` lo confirma al llegar el primer `accessToken` válido — red de
> seguridad, idempotente y sin lectura extra, porque el documento ya está leído.
> **No lo vuelvas a mover** sin releer esto.

**`alexa_refresh_tokens/{sha256DelToken}`** — el id del documento es el **hash** del token,
no el token. Si alguien se lleva un volcado de Firestore no se lleva credenciales válidas.

```
uid        string
creadoEn   Timestamp
revocado   boolean
```

### 3.3 Dónde escribe Alexa

```
lista-compra/{listaAlexa}/productos/{idAuto}
    producto     string
    fecha        Timestamp     ← Timestamp de Firestore, NO new Date()
    isImportant  false
    isPurchased  false
```

**Ojo con `fecha`:** hoy la API escribe `new Date()`. El Admin SDK lo convierte a Timestamp
al guardar, así que funciona, pero hay que usar `admin.firestore.Timestamp.now()` para que
sea explícito y coincida con lo que escribe la app. `isImportant` e `isPurchased` **deben
ir siempre**: la app hace `as? Boolean ?: false`, así que no reventaría, pero un producto
sin ellos se comporta distinto al editarlo.

---

## 4. Alcance

**Dentro:**
- Verificación de firma y de `applicationId` en el endpoint de skill.
- Servidor OAuth2 (authorize + token) en `api-devware`.
- Resolución del usuario y su lista en `AddProductIntent`.
- Reglas de Firestore para lo nuevo.
- Pantalla en la app para elegir la lista de Alexa y ver el estado de vinculación.
- Configuración y publicación de la skill.

**Fuera (no se toca en esta rama):**
- Intents nuevos (leer la lista, borrar productos, marcar comprado). Solo `AddProductIntent`
  y `CloseSkillIntent`, que es lo que ya hay.
- App-to-app account linking (vincular desde dentro de la app sin pasar por la app de Alexa).
  Es una mejora de UX posterior; requiere el mismo OAuth ya montado, así que no se pierde nada.
- Google Assistant / Siri Shortcuts.
- El campo `apiKey` muerto.

---

## 5. Reglas de arquitectura

- **La app no habla con la API.** Sigue yendo directa a Firestore con el SDK de GitLive.
  La API solo la usa Alexa. No se añade cliente HTTP a la app.
- **En la app, capas como en el resto**: `datasource → repository → usecase → viewmodel`.
  Nada de tocar Firestore desde un ViewModel.
- **Esto es KMP**: todo lo de la app va en `commonMain` y tiene que funcionar en Android
  **y** en iOS. Si algo necesita `expect/actual`, se dice antes de escribirlo.
- **En la API**, el patrón que ya existe: `route → controller → model`, con el model
  inyectado desde `index.js`. El servidor OAuth es un router más.
- **Los textos de la app van a los tres idiomas** que ya soporta el proyecto. Ninguno
  hardcodeado en el composable.
- **Nadie ejecuta builds.** Compila y prueba BonyGoD.

---

## 6. Fases

### Fase 0 — Lo que solo puedes hacer tú (antes de que Sonnet empiece)

Sin esto, las fases 2 y 6 no se pueden ni probar.

**Datos ya confirmados el 22 ago 2026:**

| Dato | Valor |
|---|---|
| `<API_BASE_URL>` | `https://api-devware.vercel.app` (verificado: `POST /alexa` responde 200 en 0,5 s en caliente) |
| Proyecto Vercel | `devware-projects/api-devware` |
| Proyecto Firebase | `lista-compra-6d7b6` |
| `authDomain` | `lista-compra-6d7b6.firebaseapp.com` |
| Skill | *Compra*, Español (ES), personalizada, creada el 29 dic 2025, en desarrollo |
| Skill ID | `amzn1.ask.skill.95b5eb87-5c1b-470a-9e9d-f0465db7a038` |
| Vendor ID | `M60EDEJOQRMPD` |
| Nombre de invocación | `lista de la compra` — **válido** (4 palabras, minúsculas, sin wake words). Pero ver riesgo 6.2 bis. |

**La app Web de Firebase ya existe.** Vercel tiene `FIREBASE_API_KEY`, `FIREBASE_AUTH_DOMAIN`,
`FIREBASE_PROJECT_ID`, `FIREBASE_STORAGE_BUCKET`, `FIREBASE_MESSAGING_SENDER_ID`,
`FIREBASE_APP_ID` y `FIREBASE_MEASUREMENT_ID`: ese es exactamente el objeto de configuración
que Firebase entrega al registrar una app **Web** (`measurementId` solo existe en web).
Así que el paso 3 está hecho y **la página de authorize reutiliza esas variables** — no se
crean `FIREBASE_WEB_*` nuevas. `firebaseService.js` hoy solo lee `PROJECT_ID`, `CLIENT_EMAIL`
y `PRIVATE_KEY`; las otras cinco están sin usar y ahora encuentran destino.

Pendiente:

1. ~~Confirmar el desajuste de la sección 0.3~~ — **hecho**, ver 0.3.
2. ~~URL de la API~~ — **hecha**.
3. ~~Registrar app Web en Firebase~~ — **ya existía** (`lista-compra`). Su config es la que
   está en Vercel y es la que se usa. El 22 ago se registró además una app web
   *OAuth Alexa* que resultó **innecesaria**: `apiKey` y `authDomain` son del **proyecto**,
   no de la app (solo cambian `appId` y `measurementId`, que son de Analytics), así que las
   dos sirven igual. *OAuth Alexa* se borra sin efecto sobre usuarios ni datos.
   `FIREBASE_AUTH_DOMAIN` = `lista-compra-6d7b6.firebaseapp.com`, **confirmado el 22 ago 2026**.
4. ~~Añadir `api-devware.vercel.app` a Authorized domains~~ — **hecho el 22 ago 2026**.
   (Sin ese dominio, el login con Google desde la página de authorize fallaría con
   `auth/unauthorized-domain`.)
5. ~~Skill ID~~ y ~~redirect URIs~~ — **hechos**, tabla de arriba.
6. ~~Nombre de invocación~~ — **válido**, pero hay que probar la colisión de 6.2 bis.
7. **Inventar `ALEXA_CLIENT_ID` y `ALEXA_CLIENT_SECRET`** (dos cadenas aleatorias largas)
   y **generar `ALEXA_JWT_SECRET`** (32 bytes aleatorios). Marcarlas **Sensitive** al crearlas.
8. **Rotar `FIREBASE_PRIVATE_KEY` y volver a crearla como Sensitive.** Ver 0.7.
9. **NO tocar "Distribuir" todavía.** La skill se queda "En desarrollo" hasta la fase 6.8.
   En ese estado ya se puede probar con la cuenta de Amazon del desarrollador.

Variables de entorno **nuevas** en Vercel (cinco). Solo `ALEXA_CLIENT_SECRET` y
`ALEXA_JWT_SECRET` son secretos de verdad y se marcan **Sensitive**; las otras tres son
identificadores públicos y conviene dejarlas legibles para poder verificarlas luego:

```
ALEXA_SKILL_ID          amzn1.ask.skill.95b5eb87-5c1b-470a-9e9d-f0465db7a038
ALEXA_CLIENT_ID         (inventado)
ALEXA_CLIENT_SECRET     (inventado)
ALEXA_JWT_SECRET        (32 bytes aleatorios)
ALEXA_REDIRECT_URIS     https://alexa.amazon.co.jp/api/skill/link/M60EDEJOQRMPD,https://pitangui.amazon.com/api/skill/link/M60EDEJOQRMPD,https://layla.amazon.com/api/skill/link/M60EDEJOQRMPD
```

---

### Fase 1 — Blindar el endpoint de skill (repo `api-devware`) — ✅ **HECHA**

**Terminada, desplegada y probada en producción el 22 ago 2026** (v1.1.0).

Cómo quedó y qué se aprendió:

- Se añadieron **`ask-sdk-express-adapter` y `ask-sdk-core`**, las dos pineadas a `2.14.0`.
  `ask-sdk-core` no es un peer dependency de papel: hay `require("ask-sdk-core")` real en
  `dist/index.js` y en `dist/verifier/index.js`, así que tiene que estar declarada.
- **No hay conflicto con Express 5**: el paquete no hace `require('express')` en ningún
  sitio; los tipos de Express desaparecen al compilar.
- El paquete es CommonJS y el repo es ESM, pero su `dist/index.js` usa
  `Object.defineProperty(exports, ...)`, que el analizador de Node sí resuelve. Los imports
  con nombre funcionan. (Si hubiera usado `__exportStar` habrían fallado en el arranque, y
  se habría caído la API entera, no solo `/alexa`.)
- La comprobación de `ALEXA_SKILL_ID` va **antes** de verificar la firma: no tiene sentido
  descargar la cadena de certificados de Amazon para acabar fallando por una variable.
- Al mergear `main` en `develop` hubo **conflicto en `apiDevWare.js`**: fuera de los
  marcadores quedaba un `app.use(json())` sin `verify`. Resuelto combinando los dos lados
  — orden de `main` (cors antes que json) y el `verify` de esta fase, en una sola llamada.
- El paso 4 original (limpiar documentos huérfanos) se descartó: ver 0.3.

Bloqueante para certificar. Se hizo primero porque es independiente de todo lo demás.

1. **Instalar `ask-sdk-express-adapter`** (verificadores oficiales de Amazon).

2. **Conservar el body crudo.** Es el paso que más gente rompe: la verificación de firma
   necesita **los bytes exactos** del body, y `express.json()` los descarta al parsear.
   En `api/apiDevWare.js`, cambiar:

   ```js
   app.use(json());
   ```

   por:

   ```js
   app.use(json({ verify: (req, res, buf) => { req.rawBody = buf; } }));
   ```

   Es inocuo para las rutas de mail y fotos.

3. **Middleware `api/middlewares/alexaVerify.js`**, aplicado **solo** al router de Alexa:
   - `new SkillRequestSignatureVerifier().verify(req.rawBody.toString(), req.headers)`
   - `new TimestampVerifier().verify(req.rawBody.toString())` (tolerancia 150 s)
   - Comparar `req.body.context.System.application.applicationId` con `ALEXA_SKILL_ID`.
   - Cualquier fallo → **HTTP 401** y log. No devolver respuesta de Alexa: si no viene de
     Alexa, no hay a quién responder.
   - **Escotilla de desarrollo:** si `process.env.ALEXA_SKIP_VERIFY === 'true'`, saltar la
     verificación. Esa variable **no se pone nunca en producción**; sirve para curl local.
     Que el middleware escriba un `logger.warn` bien visible cuando esté activa.

4. ~~Limpiar los documentos huérfanos de `lista-compra`~~ — **descartado el 22 ago 2026**,
   decisión de BonyGoD. Los documentos de primer nivel con campo `producto` que dejó la
   versión vieja de la API **se quedan donde están**: no los lee nadie, no estorban, y
   borrar en producción es riesgo sin ganancia.
   Queda anotado para que, cuando alguien los vea en Firestore, sepa qué son y por qué siguen ahí.

---

### Fase 2 — Servidor OAuth2 (repo `api-devware`) — ✅ **HECHA**

**Desplegada en producción y verificada de extremo a extremo el 22 ago 2026**, con un flujo
completo lanzado a mano: página de authorize → login por correo → `POST /code` → canje del
código contra `/token`.

| Comprobación | Resultado |
|---|---|
| Canje del código (HTTP Basic + PKCE S256) | 200 con `access_token`, `refresh_token`, `expires_in: 3600` |
| Payload del JWT | `{sub: <uid firebase>, typ: 'access'}`, vida de 3600 s exactos |
| Replay del mismo código | `invalid_grant` / 400 — la transacción funciona |
| Client secret incorrecto | `invalid_client` / 401 |
| `grant_type=refresh_token` | 200, access token nuevo y refresh token **rotado** |

Lo que se aprendió:

- **El `readFileSync` de `authorize.html` no es un problema**: el `includeFiles` que se añadió
  a `vercel.json` hace que Vercel empaquete la vista. La página se sirve correctamente.
- **La corrección de CORS de `main` era imprescindible**, no aseo. El navegador manda
  cabecera `Origin` incluso en POST del mismo origen, y `api-devware.vercel.app` no está en
  `ACCEPTED_ORIGINS`. La versión de `main` responde `callback(null, false)` — sigue adelante
  sin cabeceras CORS, que es lo correcto para una petición del mismo origen. La versión vieja
  de `develop` hacía `callback(new Error(...))`, que habría dado un 500 en `POST /code` y roto
  la vinculación entera con un error opaco.
- **`markUserLinked` no puede tumbar la respuesta de token.** Se corrigió en revisión: iba
  dentro de un `Promise.all` sin `catch`, y para cuando se ejecuta el código de autorización
  ya está consumido. Un fallo de Firestore en un campo *cosmético* habría dejado la
  vinculación rota sin reintento posible.
- **Tiempos**: 1,85 s el primer canje (función tibia), 0,47 s el refresh. Dentro del límite de
  4,5 s de Amazon, pero con menos margen del deseable en frío. Vigilar si aparecen fallos
  intermitentes de vinculación.
- Un flujo lanzado a mano **termina en un 400 de Amazon** en `accountLink/establish`, y es
  lo esperado: Amazon no reconoce un `state` que no emitió él. Todo lo anterior sí es válido.

**Pendiente de probar**: el login con Google (`signInWithPopup`) y la vinculación real desde
la app de Alexa, que es donde el popup puede fallar por ser un navegador incrustado.

---

#### Diseño original

Ficheros nuevos:

```
api/routes/alexaOAuthRoute.js
api/controllers/AlexaOAuthController.js
api/models/alexaOAuthModel.js
api/views/authorize.html        ← página de login
```

Montado en `apiDevWare.js` como `app.use('/alexa/oauth', createAlexaOAuthRouter({ alexaOAuthModel }))`.

**Importante: el router de OAuth va montado FUERA del router de Alexa**, porque estas rutas
las llama un navegador y el servicio de Alexa, no el runtime de skills — el middleware de
firma de la fase 1 **no** se les aplica.

#### 2.1 `GET /alexa/oauth/authorize`

Recibe `client_id`, `redirect_uri`, `state`, `response_type=code`, `scope`,
`code_challenge`, `code_challenge_method=S256`.

- Validar `client_id` contra `ALEXA_CLIENT_ID` y `redirect_uri` contra la lista de
  `ALEXA_REDIRECT_URIS`. **Comparación exacta, no `startsWith`** — un `startsWith` mal
  puesto aquí es un open redirect.
- Rechazar si `code_challenge_method !== 'S256'`.
- Servir `authorize.html` con los parámetros inyectados en un `<script>` como JSON.

**La página** (HTML plano, Firebase Web SDK por CDN, configurado con las variables
`FIREBASE_API_KEY`, `FIREBASE_AUTH_DOMAIN`, `FIREBASE_PROJECT_ID` y `FIREBASE_APP_ID`
**que ya están en Vercel** — ver fase 0):
- Login con correo/contraseña y con Google — los dos proveedores que usa la app.
- Texto claro: "Vincula tu cuenta de ListaCompra con Alexa".
- Al autenticar, `getIdToken()` y `POST` a `/alexa/oauth/code`.
- Manejar el caso de credenciales incorrectas sin dejar la página en blanco.

> **Los usuarios anónimos no pueden vincular.** Una sesión anónima vive solo en el
> dispositivo; no hay forma de reproducirla en un navegador. La página debe decirlo con
> todas las letras: *"Necesitas una cuenta con correo o Google. Puedes crearla desde la
> app en Ajustes."* Y la app tiene que avisar antes (fase 5).

#### 2.2 `POST /alexa/oauth/code`

Body: `{ idToken, clientId, redirectUri, state, codeChallenge }`.

- `admin.auth().verifyIdToken(idToken)` → `uid`. Si falla → 401.
- Rechazar si el usuario es anónimo (`decoded.firebase.sign_in_provider === 'anonymous'`).
- Generar `code` (`crypto.randomBytes(32).toString('hex')`).
- Guardar en `alexa_oauth_codes/{code}` con `expiraEn` = ahora + 10 min y `usado: false`.
- Devolver `{ redirectTo: "<redirect_uri>?code=<code>&state=<state>" }` y que el navegador
  haga el salto.

#### 2.3 `POST /alexa/oauth/token`

Content-type `application/x-www-form-urlencoded` — **hace falta `express.urlencoded()`**,
que ahora mismo no está montado en la API. Añadirlo.

`grant_type=authorization_code`:
- Validar `client_id` + `client_secret` (llegan en el body o en `Authorization: Basic`).
  Comparar con `crypto.timingSafeEqual`.
- **PKCE:** `base64url(sha256(code_verifier)) === codeChallenge` guardado. Si no, 400.
- **Consumir el código en una transacción de Firestore** (`runTransaction`), comprobando
  `usado === false` y `expiraEn > ahora`, y marcándolo `usado: true` dentro de la misma
  transacción. Sin transacción, dos peticiones simultáneas canjean el mismo código.
- Emitir:
  - `access_token`: JWT HS256 firmado con `ALEXA_JWT_SECRET`, payload `{ sub: uid, typ: 'access' }`, `expiresIn: '1h'`.
  - `refresh_token`: `crypto.randomBytes(32).toString('hex')`, guardado **hasheado**
    (`sha256`) como id de documento en `alexa_refresh_tokens`.
- Marcar `usuarios/{uid}` con `alexaVinculada: true` y `alexaVinculadaEn` (merge).
- Responder `{ access_token, token_type: "bearer", expires_in: 3600, refresh_token }`.

`grant_type=refresh_token`:
- Hashear el token recibido, buscar el documento, comprobar `revocado === false`.
- Emitir un access token nuevo. **Rotar el refresh token** (emitir uno nuevo y revocar el
  viejo) es lo correcto; si complica, dejarlo sin rotar y anotarlo como deuda.

Errores en formato OAuth2: `{ "error": "invalid_grant" }` con el HTTP adecuado. Amazon
espera esa forma, no un JSON propio.

#### 2.4 Limpieza

Los códigos caducados se quedan. Poner un **TTL policy en Firestore** sobre el campo
`expiraEn` de `alexa_oauth_codes` (Firebase Console → Firestore → TTL). Es gratis y
automático; no montes un cron.

---

### Fase 3 — Resolver el usuario en el intent (repo `api-devware`)

#### 3.1 `AlexaController.handleIntent`

Antes de procesar `AddProductIntent`:

```js
const accessToken = alexaRequest.context?.System?.user?.accessToken;
```

- **Si no hay token** → responder con tarjeta `LinkAccount`:

  ```js
  {
    version: '1.0',
    response: {
      outputSpeech: { type: 'PlainText', text: ALEXA_RESPONSES.NOT_LINKED },
      card: { type: 'LinkAccount' },
      shouldEndSession: true
    }
  }
  ```

  Texto: *"Para usar tu lista de la compra, vincula tu cuenta desde la app de Alexa."*
  Esa tarjeta hace que la app de Alexa muestre el botón de vincular. Es el
  comportamiento que espera certificación.

- **Si hay token pero el JWT es inválido o ha caducado** → misma respuesta. Alexa refresca
  solo con el refresh token; si aun así llega caducado, es que la vinculación se rompió.

- **Si es válido** → `uid` del `sub`.

Esto va en `LaunchRequest` también: si abres la skill sin vincular, la bienvenida no
puede ser "¿qué quieres añadir?".

#### 3.2 `AlexaModel.addProduct({ uid, producto })`

```js
1. Leer usuarios/{uid}. Si no existe → error "cuenta no encontrada".
2. listaId = doc.listaAlexa || doc.listas?.[0]
   Si no hay ninguna → error "no tienes ninguna lista".
3. db.collection('lista-compra').doc(listaId).collection('productos').add({
     producto,
     fecha: admin.firestore.Timestamp.now(),
     isImportant: false,
     isPurchased: false
   })
```

Nuevos textos en `constantes.js`: `NOT_LINKED`, `NO_LIST`, `ACCOUNT_NOT_FOUND`.

#### 3.3 Intents obligatorios

`AMAZON.HelpIntent`, `AMAZON.StopIntent` y `AMAZON.CancelIntent` **no están manejados**:
hoy caen en `UNKNOWN_INTENT`. Certificación los prueba siempre, así que tal cual está
**es un rechazo seguro**. Añadirlos a `ALEXA_INTENTS` y al controlador.

#### 3.4 Borrar `getProducts()` y `deleteProduct()` de `alexaModel.js`

---

### Fase 4 — Reglas de Firestore (a mano en la consola)

**No hay `firestore.rules` en el repo** — igual que pasó en el plan de onboarding, esto se
aplica a mano en Firebase Console. Sonnet deja el bloque escrito en el plan; lo pega BonyGoD.

```
match /alexa_oauth_codes/{code} {
  allow read, write: if false;
}
match /alexa_refresh_tokens/{tokenHash} {
  allow read, write: if false;
}
```

`if false` es correcto y es lo que queremos: **el Admin SDK se salta las reglas**, así que
la API sigue funcionando y ningún cliente puede leer ni escribir ahí.

En `usuarios/{uid}` el dueño ya puede escribir su documento, así que `listaAlexa` funciona
sin cambios. Eso significa que un usuario podría falsificarse `alexaVinculada: true` en su
propio documento — es un campo **cosmético** (solo pinta un estado en la app), no da acceso
a nada. Se acepta y queda anotado.

---

### Fase 5 — App: elegir lista y ver estado (repo `ListaCompraApp`)

> **Requisito heredado de la fase 3:** `AlexaModel.addProduct` escribe en
> `lista-compra/{listaAlexa}/productos` **sin comprobar que esa lista siga existiendo** — en
> Firestore una subcolección puede vivir bajo un documento borrado, así que apuntar a una
> lista muerta haría que los productos dictados desaparecieran en silencio. Se decidió no
> añadir una lectura extra en la ruta crítica (límite de 8 s de Alexa) y resolverlo aquí:
> **cuando la app borre una lista, debe limpiar o repuntar `listaAlexa`** si apuntaba a ella.

#### 5.1 Capa de datos

- `UserResponse` y el modelo de dominio: añadir `listaAlexa: String` y `alexaVinculada: Boolean`.
- `UsersDataSource`: `getConfigAlexa()`, `setListaAlexa(listaId: String)`.
- Repositorio + casos de uso siguiendo el patrón de `SetDefaultListaUseCase`, que hace
  justo lo mismo sobre el mismo documento — cópialo como referencia.

#### 5.2 UI

Una sección **"Alexa"** en ajustes (o pantalla propia; decide dónde encaja mejor con la
navegación que ya hay, y dilo antes de escribirla):

- **Usuario anónimo** → no mostrar el selector. Mostrar el aviso de que hace falta una
  cuenta y el enlace a vincular cuenta que ya existe de la rama de onboarding.
- **Usuario con cuenta, sin vincular** → explicar en dos frases que se vincula desde la
  app de Alexa (Más → Skills → buscar la skill → Vincular cuenta). No hay forma de lanzarlo
  desde aquí en esta versión.
- **Vinculado** → selector de a qué lista escribe Alexa, alimentado por `getListas()`,
  que ya existe.

Todo el texto a los tres idiomas.

#### 5.3 Comprobación en iOS

El cambio es Firestore puro en `commonMain`, sin `expect/actual`, así que no debería dar
guerra. Pero se prueba en los dos, como siempre.

---

### Fase 6 — Configurar y publicar la skill

Esto lo hace BonyGoD en el Alexa Developer Console. Sonnet no puede.

#### 6.1 Crear la skill — **ya hecho**

Existe la skill *Compra*, personalizada, Español (ES), creada el 29 dic 2025, en estado
**En desarrollo**. Eso es exactamente lo que hace falta: en desarrollo ya se puede probar
con la cuenta de Amazon del desarrollador, incluido el account linking.

Solo queda copiar el **Skill ID** (`amzn1.ask.skill.…`) → `ALEXA_SKILL_ID`.

Antes de certificar conviene revisar el **nombre público**: *Compra* a secas es genérico y
no dice que haya app detrás. Algo como *Lista del Súper* posiciona mejor en la Skill Store
y cuadra con la ficha de Play. Se puede cambiar hasta el momento de enviar a certificación.

#### 6.2 Nombre de invocación

Es lo que dice el usuario para abrirla: *"Alexa, abre **lista del súper**"*.

Reglas que provocan rechazo si se saltan: dos palabras o más, en minúsculas, sin nombres
de marca ajenos, sin las palabras de activación (`alexa`, `echo`, `computer`, `amazon`),
sin números ni signos.

**El actual es `lista de la compra`** y cumple todas esas reglas: cuatro palabras, minúsculas,
sin wake words, sin números. Como nombre, es válido.

#### 6.2 bis — Riesgo real: choca con la lista nativa de Alexa

**Alexa ya tiene listas de la compra propias en español.** *"Alexa, añade leche a la lista
de la compra"* es una frase que el asistente **ya resuelve de forma nativa**, sin skills.

Eso no bloquea el nombre — en amazon.es hay skills publicadas llamadas literalmente
*"Lista de la compra"*, así que Amazon lo permite. El problema no es de certificación,
es de **uso real**: las frases one-shot, que son las cómodas y las que la gente va a usar,
tienen muchas papeletas de aterrizar en la lista nativa de Amazon en vez de en tu skill.
El usuario diría "añade leche", vería un "vale" de Alexa, y el producto no aparecería en
tu app. El peor fallo posible: silencioso y con cara de haber funcionado.

**Hay que medirlo antes de construir nada encima**, y se puede hoy mismo porque la skill ya
existe en desarrollo. En un Echo con tu cuenta de desarrollador, probar y anotar qué contesta:

| Frase | ¿Nativa o skill? |
|---|---|
| *"Alexa, abre lista de la compra"* | |
| *"Alexa, añade leche a la lista de la compra"* | |
| *"Alexa, pídele a lista de la compra que añada leche"* | |
| *"Alexa, añade leche"* (con la skill abierta) | |

- Si la nativa se come las one-shot → **cambiar el nombre de invocación** a algo que no
  colisione: *"lista del súper"*, *"mi súper"*, *"la cesta"*. Se pierde la frase natural
  pero se gana que funcione. *"lista del súper"* además **coincide con el título de la
  ficha de Play**, que refuerza las dos cosas.
- Si conviven, se queda como está.

Esta prueba **no bloquea la fase 1** (la firma y el `applicationId` no dependen del nombre),
pero conviene hacerla pronto: puede cambiar el nombre de la skill, y eso arrastra el nombre
público, la descripción y las capturas de la fase 6, además de los textos de la fase 5.
Cuanto más tarde se descubra, más hay que rehacer.

#### 6.2 ter — Por qué no se puede sincronizar con la lista nativa

La duda evidente: si Alexa ya tiene listas, ¿por qué no escribir en ellas?

Porque no se puede. **Amazon apagó las List Skills y la List Management REST API el 1 de
julio de 2024.** Es lo que dejó a AnyList y compañía sin sincronización con Alexa. Una skill
personalizada con backend propio — exactamente lo que hace este plan — es el único camino
que queda. La arquitectura es la correcta; no hay un atajo que nos estemos saltando.

#### 6.3 Modelo de interacción

Intent `AddProductIntent` con slot `producto`.

Para el tipo de slot: **crea un slot personalizado** (`LISTA_PRODUCTOS`) sembrado con 50-100
productos habituales (leche, pan, huevos, tomates…). Alexa extiende la resolución más allá
de los ejemplos, así que no hace falta ser exhaustivo. `AMAZON.SearchQuery` captura texto
libre mejor, pero **no se puede combinar con otros slots en la misma frase** y complica el
modelo — para un slot único no compensa.

Utterances (mínimo 15-20, cuantas más variantes naturales mejor):

```
añade {producto}
añade {producto} a la lista
añade {producto} a la lista de la compra
apunta {producto}
apunta {producto} en la lista
mete {producto} en la lista
necesito {producto}
compra {producto}
tengo que comprar {producto}
agrega {producto}
```

`CloseSkillIntent` ya lo tienes. Los obligatorios (`AMAZON.HelpIntent`, `AMAZON.StopIntent`,
`AMAZON.CancelIntent`) se manejan en la fase 3.3.

#### 6.4 Endpoint

- Tipo: **HTTPS**.
- URL por defecto: `https://api-devware.vercel.app/alexa`.
- Certificado: *"My development endpoint is a sub-domain of a domain that has a wildcard
  certificate from a certificate authority"* (es el caso de Vercel).

#### 6.5 Account Linking

Build → **Account Linking**:

Configurado el 22 ago 2026. Estado de los interruptores de *Settings*, que **no hay que
tocar** porque cada uno responde a una decisión del plan:

| Ajuste | Estado | Motivo |
|---|---|---|
| Do you allow users to create an account or link… | **ON** | interruptor maestro |
| Allow users to enable skill without account linking | **ON** | **de esto depende la fase 3**: la tarjeta `LinkAccount` solo tiene sentido si se puede activar la skill sin vincular |
| Link account from within your application or website | **OFF** | app-to-app linking, fuera de alcance (sección 4) |
| Authenticate using your mobile application | **OFF** | ídem; necesitaría package name + huellas en Android y universal links en iOS |
| Link their account using voice | **OFF** | es el "dicta un código", descartado en la sección 2 |

- Tipo: **Auth Code Grant**.
- Authorization URI: `https://api-devware.vercel.app/alexa/oauth/authorize`
- Access Token URI: `https://api-devware.vercel.app/alexa/oauth/token`
- Client ID / Client Secret: los que inventaste en la fase 0.
- Client Authentication Scheme: **HTTP Basic**. Elegido el 22 ago 2026, así que
  `POST /alexa/oauth/token` **debe leer las credenciales de la cabecera
  `Authorization: Basic base64(client_id:client_secret)`**, no del body.
- Scope: `lista`
- **PKCE Authorization: activado.** No es opcional: el servidor de 2.1 rechaza cualquier
  autorización sin `code_challenge_method=S256`, así que si el interruptor está apagado
  Amazon no manda el challenge y fallan todas las vinculaciones. Los dos lados a juego.
- Default Access Token Expiration Time: `3600`. Es el valor de reserva por si la respuesta
  de token no trae `expires_in` (la nuestra sí lo trae). Mínimo que acepta Amazon: 360.
- **Copia las tres Redirect URLs que muestra la página** → `ALEXA_REDIRECT_URIS`.
  Cambian por cuenta de desarrollador; no las inventes.
- **Domain List: pendiente, se cierra en la fase 2.** Autoriza los dominios externos que
  carga la página de authorize dentro del navegador de la app de Alexa. La nuestra usará el
  SDK web de Firebase y hablará con Google, así que probablemente haga falta `www.gstatic.com`,
  `apis.google.com`, `accounts.google.com`, `identitytoolkit.googleapis.com`,
  `securetoken.googleapis.com` y `lista-compra-6d7b6.firebaseapp.com`. **No rellenarlo a
  ciegas**: al terminar 2.1, abrir la página, mirar en el inspector qué carga de verdad, y
  poner solo eso.

#### 6.6 Distribución

- Nombre público, descripción breve (160 car.) y detallada.
- **Frases de ejemplo** (3). Deben funcionar literalmente o es rechazo.
- Iconos: **108×108** y **512×512** PNG.
- **Política de privacidad**: ya la tienes publicada en GitHub Pages (`docs/privacy-policy.html`).
  Pon esa URL.
- Términos de uso: opcional pero recomendable.
- Idioma: español.

> **Esto es lo que te interesa para descargas:** la descripción de la skill puede mencionar
> y enlazar la app. Alguien que instala la skill es tráfico cualificado de verdad.
> Aprovéchalo — que la descripción diga explícitamente que hay app para ver y gestionar
> la lista.

#### 6.7 Privacy & Compliance

- ¿Recoge información personal? **Sí** (correo, para identificar la cuenta).
- ¿Dirigida a niños? **No**.
- ¿Contiene publicidad? **No** (la skill no; la app sí, pero se pregunta por la skill).
- Cumplimiento de exportación: aceptar.

#### 6.8 Certificación

Test → validación → **Submit for certification**. Suele tardar de 2 a 5 días hábiles.

Rechazos más habituales, todos evitables:
- `HelpIntent`/`StopIntent`/`CancelIntent` sin manejar → **fase 3.3 lo cubre**.
- Frases de ejemplo que no funcionan tal cual.
- El account linking no completa el flujo (probar de verdad antes de enviar).
- La skill responde algo cuando el usuario no ha vinculado, en vez de sacar la tarjeta
  `LinkAccount` → **fase 3.1 lo cubre**.
- Política de privacidad inaccesible.

---

## 7. Riesgos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| **Arranque en frío de Vercel** + init de Firebase Admin cerca del límite de Amazon (8 s la skill, **4,5 s el endpoint de token**) | Vinculación que falla de forma intermitente y difícil de reproducir | Medir el peor caso con la función fría antes de certificar. `firebaseService` ya es singleton, que ayuda. Si va justo, considerar Vercel Fluid/warm o mover el endpoint. |
| `express.json()` sin `verify` | La firma no se puede validar y todo el trabajo de la fase 1 no sirve | Es el paso 2 de la fase 1, explícito |
| Los anónimos no pueden vincular | Confusión justo en los usuarios nuevos, que ahora entran sin registro | Aviso claro en la app (5.2) y en la página de authorize (2.1) |
| Descarga del cert chain de Amazon en cada arranque en frío | Latencia extra | El verificador oficial cachea en memoria; medir |
| El `state` de OAuth no se propaga | Alexa rechaza el callback | Está en 2.2; probar el flujo completo, no solo los endpoints sueltos |
| Códigos de autorización canjeados dos veces | Vinculación duplicada | Transacción de Firestore (2.3) |
| **El nombre de invocación choca con la lista nativa de Alexa** | Alto — las frases one-shot van a la lista de Amazon, el usuario cree que funcionó y el producto nunca llega a la app | Prueba de 6.2 bis **antes de la fase 1**. Si colisiona, cambiar el nombre |
| `FIREBASE_PRIVATE_KEY` sin marcar Sensitive en Vercel | Alto — da acceso total a Firestore saltándose las reglas | Rotar y recrear como Sensitive (0.7). No depende de este plan: hacerlo ya |
| Secretos en el historial de `api-devware` | Bajo — el repo es privado | El `.env` estuvo trackeado hasta `80b7fdb`. **Si alguna vez haces público ese repo, rota antes la private key de Firebase y los tokens de correo.** |

---

## 8. Criterios de aceptación

1. Un POST a `https://api-devware.vercel.app/alexa` sin firma válida devuelve **401**.
2. Un POST con firma válida pero `applicationId` ajeno devuelve **401**.
3. Abrir la skill sin vincular devuelve tarjeta `LinkAccount` y no escribe en Firestore.
4. El flujo de vinculación desde la app de Alexa termina en "Cuenta vinculada correctamente".
5. Tras vincular, *"añade leche"* crea el producto en `lista-compra/{listaAlexa}/productos`
   **de ese usuario**, con `fecha`, `isImportant` e `isPurchased`.
6. El producto aparece en la app **en tiempo real**, sin reabrirla.
7. **Dos cuentas distintas escriben en listas distintas.** Es la prueba que da nombre al plan.
8. Cambiar la lista de Alexa en la app hace que el siguiente producto vaya a la nueva.
9. Un usuario anónimo ve el aviso y no ve el selector.
10. Un código de autorización usado dos veces devuelve `invalid_grant`.
11. Al caducar el access token, Alexa refresca sola y el usuario no nota nada.
12. `HelpIntent`, `StopIntent` y `CancelIntent` responden algo sensato.
13. Android e iOS, los dos.

---

## 9. Guion de pruebas

**Decisión del 22 ago 2026:** las pruebas se acumulan y se pasan **todas de una vez al final**,
con las fases 1 a 5 hechas y desplegadas. Este bloque es la lista definitiva de esa sesión.

### Ya verificado sobre producción (no hace falta repetirlo)

| # | Qué | Cuándo |
|---|---|---|
| ✅ | `POST /alexa` con `{}` → **401** (antes 200) | 22 ago |
| ✅ | `POST /alexa` con envelope válido y `applicationId` correcto, **sin firma** → 401 → luego el corte lo hace la firma, no el body | 22 ago |
| ✅ | Una petición **real de Alexa** atraviesa el middleware (la skill contesta al abrirla) → firma y `applicationId` válidos pasan | 22 ago |
| ✅ | `/fotos` responde 200 y el formulario de `devware.es` envía correo → el body parser global no rompió nada | 22 ago |
| ✅ | Página de authorize se sirve → Vercel empaqueta `authorize.html`, el `readFileSync` no es un problema | 22 ago |
| ✅ | Login por **correo** en navegador de escritorio → `POST /code` devuelve `redirectTo` | 22 ago |
| ✅ | Canje del código (HTTP Basic + PKCE S256) → 200 con `access_token`, `refresh_token`, `expires_in: 3600` | 22 ago |
| ✅ | Payload del JWT: `{sub: <uid>, typ: 'access'}`, vida de 3600 s exactos | 22 ago |
| ✅ | **Replay del mismo código** → `invalid_grant` / 400 | 22 ago |
| ✅ | Client secret incorrecto → `invalid_client` / 401 | 22 ago |
| ✅ | `grant_type=refresh_token` → access token nuevo y refresh token **rotado** | 22 ago |
| ✅ | `usuarios/{uid}.alexaVinculada === true` tras el canje → `markUserLinked` funciona | 22 ago |

Nota: un flujo lanzado a mano termina en un **400 de Amazon** en `accountLink/establish`.
Es lo esperado —Amazon no reconoce un `state` que no emitió él— y no invalida lo anterior.

### Preparación
- Dos cuentas de prueba distintas, cada una con al menos una lista.
- Un dispositivo Echo o la app de Alexa en el móvil.
- La consola de Firestore abierta.
- Fases 1 a 5 desplegadas.

### Bloque 1 — Vinculación real desde la app de Alexa
1. Háblale a la skill **sin estar vinculado** → responde `NOT_LINKED` y **la app de Alexa
   pinta el botón de vincular** (es el camino de usuario, no hay que buscar ningún menú).
2. Pulsar ahí → sale la página de login → entrar con **correo** → "Cuenta vinculada correctamente".
3. Firestore: hay un doc en `alexa_refresh_tokens` y el de `alexa_oauth_codes` está `usado: true`.
4. Desvincular y repetir con **Google**. ⚠️ Es la prueba de `signInWithPopup` dentro del
   navegador incrustado de Amazon: si falla aquí y el correo sí funciona, el problema es el
   popup, y la alternativa es `signInWithRedirect`.
5. Intentar vincular estando en **sesión anónima** → sale el aviso, no deja seguir.

### Bloque 2 — Uso
6. *"Alexa, abre lista de la compra"* → bienvenida (ya no la tarjeta de vincular).
7. *"añade leche"* → confirma y aparece en la app **sin recargar**.
8. Firestore: el producto está en `lista-compra/{listaAlexa}/productos` con `fecha` como
   Timestamp, `isImportant: false` e `isPurchased: false`.
9. Producto de dos palabras: *"añade papel de cocina"* → entra completo, no truncado.
10. *"ayuda"*, *"para"* y *"cancela"* → responden y cierran bien (obligatorio para certificar).

### Bloque 3 — Colisión del nombre de invocación (6.2 bis)
11. *"Alexa, añade leche a la lista de la compra"* **en frío** → ¿contesta tu skill o la lista
    nativa de Amazon? Se distingue por el texto y por dónde acaba el producto (Firestore vs.
    app de Alexa → Listas).
12. *"Alexa, pídele a lista de la compra que añada leche"* → si esta sí funciona y la 11 no,
    el problema es la gramática one-shot, no el nombre, y cambiarlo no arreglaría nada.

### Bloque 4 — Multiusuario (lo importante)
13. Vincular la **segunda cuenta** en otro dispositivo/cuenta de Amazon.
14. Añadir un producto desde cada una.
15. **Cada producto en su lista. Ninguna cuenta ve el de la otra.**

### Bloque 5 — Lista y caducidad
16. En la app, cambiar la lista de Alexa a una segunda lista.
17. *"añade huevos"* → va a la nueva.
18. Borrar en la app la lista a la que apunta `listaAlexa` y volver a dictar → **no debe
    perderse en silencio** (ver el requisito heredado en la fase 5).
19. Esperar a que caduque el access token (>1 h) y volver a añadir → funciona sin intervención.

### Bloque 6 — iOS
20. Repetir 7, 16 y 17 con la app de iOS.

---

## 10. Cómo trabajamos

- **Fase a fase.** Sonnet implementa una, para, y se revisa antes de seguir.
- El orden importa: **0 → 1 → 2 → 3 → 4 → 5 → 6**. La fase 6 necesita la 2 desplegada
  para poder probar el linking, pero el paso 6.1 (crear la skill) va en la fase 0 porque
  hace falta el Skill ID.
- **Nadie ejecuta builds ni `gradlew`.** Compila y prueba BonyGoD.
- Si algo del plan no encaja al escribirlo, **se dice antes de desviarse**, y la desviación
  se anota aquí — como se hizo con el interruptor del intersticial en el plan anterior.
- El grafo de graphify se actualiza **al terminar la rama**, no por fase.
- Este documento se va marcando con el estado por fase, igual que
  `PLAN-ONBOARDING-SIN-REGISTRO.md`.
