# Plan — pedir valoración en las stores

Pedir al usuario que valore la app, con la API nativa de cada plataforma, en el momento
en que acaba de terminar la compra.

Rama: `feature/valoracion-stores`, desde `develop`.

---

## 1. Qué problema resuelve, y cuál no

La ficha de una app con 4 reseñas convierte peor que la misma ficha con 20. Es de lo más
barato que se puede tocar para mejorar la conversión de quien ya ha llegado a la ficha.

**Lo que no hace: traer gente a la ficha.** Con 77 usuarios esto da un puñado de reseñas,
no treinta. Ayuda a que quien llega se instale; no aumenta quién llega. Lo que mueve eso es
certificar la skill y publicar la ficha nueva, que ya está escrita y parada.

Conviene tenerlo claro antes de medir el resultado y llevarse una decepción.

---

## 2. Decisiones cerradas

### 2.1 API nativa, no un diálogo propio

| Plataforma | API |
|---|---|
| Android | Play In-App Review — `ReviewManager` |
| iOS | `SKStoreReviewController` / `AppStore.requestReview` |

Las dos son **fire and forget**: se llaman y el sistema decide si enseña algo. Google
aplica una cuota por usuario y **no informa de si el diálogo llegó a mostrarse**; Apple lo
limita a unas 3 veces por año y usuario. No hay callback con el resultado, ni forma de
saber si el usuario valoró.

De ahí sale una regla que hay que respetar en todo el diseño: **nada puede depender de que
la valoración ocurra**. Ni contarla, ni reintentarla porque "no salió", ni desbloquear nada.

### 2.2 Nunca preguntar antes

El patrón de *"¿te gusta la app?" → si dice que sí, mandarlo a la store; si dice que no,
mandarlo a un formulario* **va contra las normas de Apple**. Es el patrón que monta casi
todo el mundo y es motivo de rechazo. No se hace.

Tampoco se incentiva de ninguna forma —ni cupones, ni funciones, ni quitar anuncios— porque
lo prohíben las dos tiendas.

### 2.3 El momento: al terminar la compra

Se pide cuando el usuario **marca como comprado el último producto que le quedaba
pendiente**. Es el momento de logro recurrente de esta app: la lista se queda a cero y
acaba de salir del súper.

Descartados, y por qué:

| Momento | Por qué no |
|---|---|
| Al abrir la app | No ha pasado nada bueno todavía |
| Al vincular Alexa | Acaba de hacer *setup*, aún no ha visto que funcione |
| En la pantalla de Alexa | Está configurando, no disfrutando |
| Al vaciar la lista entera | Ambiguo: tanto puede ser "terminé" como "esto es un desastre, borro" |
| Al ver aparecer un producto dictado a Alexa | Es el "wow" real de la feature, pero puede saltar en mitad del súper y detectarlo es frágil |

### 2.4 Dónde se guardan los contadores

En `usuarios/{uid}`, tres campos nuevos. **No se añade ninguna librería de persistencia
local.**

| Campo | Tipo | Qué es |
|---|---|---|
| `comprasCompletadas` | número | Cuántas veces ha dejado la lista a cero |
| `resenaPedidaVersion` | texto | `appVersion` de la última vez que se pidió; `""` si nunca |
| `resenaPedidaCompras` | número | Valor de `comprasCompletadas` cuando se pidió; `0` si nunca |

El documento de usuario **ya se lee en cada `loadUserDataSuspending()`**, así que leerlos
es gratis, y es exactamente el camino que se acaba de recorrer con `alexaVinculada` y
`listaAlexa`: modelo → mapper → datasource → repository → usecase.

La alternativa era meter `multiplatform-settings` o DataStore. Se descarta: una dependencia
nueva y un mecanismo de persistencia nuevo para tres enteros, cuando ya hay uno funcionando.
Además, guardarlo en el usuario hace que el contador **siga a la persona y no al teléfono**,
que para esto es lo correcto.

Coste: una escritura por compra terminada. Despreciable.

### 2.5 Cuándo se cumple la condición

Se pide si se cumplen las tres:

```
comprasCompletadas >= 3
appVersion != resenaPedidaVersion
comprasCompletadas - resenaPedidaCompras >= 10
```

O sea: **la primera vez a la tercera compra terminada**, y a partir de ahí nunca antes de
10 compras más *y* una versión nueva. Deliberadamente conservador — la cuota de Apple es de
unas 3 al año, y gastarla en alguien que todavía no sabe si le gusta la app es tirarla.

No hay regla de "días desde la instalación": tres compras terminadas ya son mejor señal de
madurez que el calendario, y no obliga a guardar una fecha de instalación que hoy no existe.

---

## 3. Fases

### Fase 1 — Los tres campos, de Firestore a la UI

- `Usuario` (dominio) y su respuesta/mapper: `comprasCompletadas`, `resenaPedidaVersion`,
  `resenaPedidaCompras`, con valores por defecto para los usuarios que ya existen y no los
  tienen.
- `UsersDataSource`: leerlos, y un método para escribirlos.
- Casos de uso: `RegistrarCompraCompletadaUseCase` y `MarcarResenaPedidaUseCase`.

**Cuidado con los usuarios existentes.** Los 77 documentos actuales no tienen estos campos.
Se leen con valor por defecto, no se migra nada, y quien ya usaba la app empieza a contar
desde cero. Es lo correcto: nadie debería recibir la petición por compras que hizo antes de
que existiera el contador.

### Fase 2 — `expect/actual` de la petición

Nuevo `core/review/RequestAppReview.kt` en `commonMain`, con sus dos `actual`. Sigue el
patrón que ya existe en `ads/` (`AdConstants`, `ShowPreloadedInterstitial`).

```kotlin
expect suspend fun requestAppReview()
```

- **Android**: `ReviewManagerFactory.create(context)`, `requestReviewFlow()`, y
  `launchReviewFlow()` con el resultado. Cualquier fallo se traga en silencio — es cosmético
  y nunca debe romper nada de la pantalla.
- **iOS**: `SKStoreReviewController.requestReview()` sobre la escena activa.

Necesita el `Context` en Android. Mirar cómo lo resuelven los anuncios en
`AdComponents.android.kt` y hacerlo igual, **no inventar un mecanismo nuevo**.

### Fase 3 — El enganche

En `ListaCompraViewModel.togglePurchased()`, después de que el toggle se haya guardado:

1. Detectar la transición: **antes había pendientes, ahora no queda ninguno**. Solo esa
   transición cuenta; una lista que ya estaba a cero no suma nada.
2. `RegistrarCompraCompletadaUseCase()` → incrementa `comprasCompletadas`.
3. Evaluar la condición de 2.5. Si se cumple: `requestAppReview()` y
   `MarcarResenaPedidaUseCase()` con la versión y el contador actuales.

**Se marca como pedida aunque no se sepa si se mostró.** No hay forma de saberlo, y
reintentar "porque no salió" es exactamente lo que quema la cuota del usuario.

**Nunca bloquea nada.** Si algo de esto falla, la lista se ha marcado igual y el usuario no
se entera. Todo el bloque va con su propio manejo de error.

### Fase 4 — Pruebas

Bloque de abajo.

---

## 4. Riesgos

| Riesgo | Qué pasa | Qué hacer |
|---|---|---|
| **En Android solo funciona instalado desde Play** | En un debug local no sale nada, y parece roto | Probar por el canal de **testing interno**. Un debug que no enseña nada **no es un fallo** |
| **La cuota no se puede forzar** | Puede no salir aunque el código sea correcto | Verificar con logs que se llega a la llamada; eso es lo único comprobable |
| **Colisión con el intersticial** | Dos interrupciones seguidas | El intersticial va al arrancar y esto al terminar la compra: están lejos. Confirmarlo en pruebas |
| **Doble conteo** | Toggle rápido de un lado a otro infla el contador | Solo cuenta la transición pendientes → cero, y solo cuando venía de tener pendientes |
| **Usuario anónimo** | También tiene documento, también cuenta | Se deja: es un usuario real usando la app |
| **La escritura falla** | El contador se queda corto | Da igual, es cosmético. Que no propague el error |

---

## 5. Criterios de aceptación

1. Marcar el último pendiente incrementa `comprasCompletadas` en Firestore.
2. Marcar y desmarcar repetidamente **no** lo incrementa más de una vez por transición real.
3. Con `comprasCompletadas < 3` no se llama a la API de valoración.
4. Al llegar a 3, se llama, y se escriben `resenaPedidaVersion` y `resenaPedidaCompras`.
5. Con la misma versión instalada no se vuelve a llamar, por muchas compras que se terminen.
6. Un error escribiendo en Firestore no impide marcar el producto ni rompe la pantalla.
7. No existe ningún diálogo propio preguntando si le gusta la app.
8. Nada en la app depende de que la valoración ocurra.
9. Funciona en Android y en iOS, todo lo compartido en `commonMain`.
10. Los usuarios que ya existen, sin los campos, arrancan en cero sin fallar.

---

## 6. Pruebas

| # | Paso | Esperado | OK |
|---|---|---|---|
| 1 | Lista con 3 productos, marcar los 2 primeros | `comprasCompletadas` no cambia | ☐ |
| 2 | Marcar el tercero | `comprasCompletadas` sube 1 | ☐ |
| 3 | Desmarcar y volver a marcar ese mismo | Sube 1 más, no dos | ☐ |
| 4 | Repetir hasta llegar a 3 | En la tercera se llama a la API | ☐ |
| 5 | Firestore | `resenaPedidaVersion` = versión actual, `resenaPedidaCompras` = 3 | ☐ |
| 6 | Terminar 5 compras más sin cambiar de versión | No se vuelve a llamar | ☐ |
| 7 | **Android, canal de testing interno** | Sale el diálogo de Play, o no sale por cuota — las dos son correctas | ☐ |
| 8 | **iOS** | Sale el de Apple. En build de desarrollo se ve pero no envía nada | ☐ |
| 9 | Cortar la red y terminar una compra | El producto se marca igual, sin error visible | ☐ |
| 10 | Usuario de los que ya existían, sin los campos | Arranca en cero, no revienta | ☐ |
| 11 | Sesión anónima | Cuenta igual que una con cuenta | ☐ |

> El paso 7 es el único que necesita subir al canal interno de Play. **Que no salga el
> diálogo no demuestra que esté roto** — puede ser la cuota. Lo que se comprueba ahí es que
> se llega a la llamada sin excepción.

---

## 7. Lo que este plan NO hace

- No enlaza a la ficha de la store con un botón propio. Solo la API nativa.
- No pregunta nada antes de pedir la valoración.
- No mide cuántas valoraciones se consiguen: **no se puede** desde la app. Eso se mira en
  Play Console y App Store Connect.
- No toca nada de Alexa, ni la ficha de las stores, ni el material gráfico.
