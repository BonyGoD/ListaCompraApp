# Pruebas — Alexa multiusuario

Guion de la sesión de pruebas, para pasar de una vez con las fases 1 a 5 desplegadas.
Referencia completa: `PLAN-ALEXA-MULTIUSUARIO.md`, sección 9.

**Antes de empezar, marca esto:**

- [ ] Fase 3 desplegada en producción (`api-devware`, rama `feature/alexa-resolver-usuario` mergeada)
- [ ] Fase 5 compilada e instalada en Android
- [ ] Fase 5 compilada e instalada en iOS
- [ ] Reglas de Firestore publicadas, con TTL sobre `alexa_oauth_codes.expiraEn`
- [ ] Dos cuentas de prueba distintas, cada una con al menos una lista
- [ ] Un Echo o la app de Alexa en el móvil
- [ ] Consola de Firestore abierta

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
| 1 | Hablar a la skill **sin estar vinculado** | Responde *"Para usar tu lista de la compra, vincula tu cuenta desde la app de Alexa"* **y la app de Alexa pinta el botón de vincular** | ☐ |
| 2 | Pulsar ese botón → entrar con **correo** | "Cuenta vinculada correctamente" | ☐ |
| 3 | Firestore | Hay doc en `alexa_refresh_tokens`; el de `alexa_oauth_codes` está `usado: true` | ☐ |
| 4 | Desvincular y repetir con **Google** | Igual que con correo | ☐ |
| 5 | Intentar vincular en **sesión anónima** | Sale el aviso, no deja seguir | ☐ |

> **El paso 4 es el que más riesgo tiene.** Es la prueba de `signInWithPopup` dentro del
> navegador incrustado de Amazon, donde los popups fallan a menudo. **Si el correo funciona
> y Google no, el problema es el popup, no el OAuth** — y la alternativa sería
> `signInWithRedirect`, que obliga a rehacer el flujo de vuelta. Anótalo tal cual lo veas.

## Bloque 2 — Uso

| # | Paso | Esperado | OK |
|---|---|---|---|
| 6 | *"Alexa, abre lista de la compra"* | Bienvenida, ya no la tarjeta de vincular | ☐ |
| 7 | *"añade leche"* | Confirma y aparece en la app **sin recargar** | ☐ |
| 8 | Firestore | Está en `lista-compra/{listaAlexa}/productos` con `fecha` como Timestamp, `isImportant: false`, `isPurchased: false` | ☐ |
| 9 | *"añade papel de cocina"* | Entra completo, no truncado a "papel" | ☐ |
| 10 | *"ayuda"* | Responde algo útil, sesión abierta | ☐ |
| 11 | *"para"* y *"cancela"* | Cierran limpio | ☐ |

> Los pasos 10 y 11 son **obligatorios para certificar**: Amazon los prueba siempre.

## Bloque 3 — Colisión del nombre de invocación

**El bloque que puede cambiar el nombre de la skill.** Hazlo en frío, sin haber abierto
la skill antes (si vienes de una sesión, di *"para"* y espera un poco).

| # | Frase | Anota quién contesta | OK |
|---|---|---|---|
| 12 | *"Alexa, añade leche a la lista de la compra"* | ¿Tu skill o la lista nativa de Amazon? | ☐ |
| 13 | *"Alexa, pídele a lista de la compra que añada leche"* | ¿Funciona? | ☐ |

**Cómo distinguirlo, dos señales independientes:**
- **La voz.** Tu skill dice *"leche añadido a la lista correctamente"*. Cualquier otra
  redacción es la nativa.
- **Dónde acaba.** Firestore → tu skill. App de Alexa → Listas → la nativa.

**Cómo se lee el resultado:**

| 12 | 13 | Qué significa | Qué hacer |
|---|---|---|---|
| ✅ | ✅ | No hay colisión | Nada, el nombre se queda |
| ❌ | ✅ | La nativa se come la frase natural, pero la fórmula explícita llega | **Probar a renombrar** a *"lista del súper"* y repetir 12 |
| ❌ | ❌ | El nombre está bloqueado | **Renombrar** obligatoriamente |
| ✅ | ❌ | Raro; repetir para descartar un fallo de reconocimiento | — |

## Bloque 4 — Multiusuario

**Es la prueba que da nombre a todo esto.**

| # | Paso | Esperado | OK |
|---|---|---|---|
| 14 | Vincular la **segunda cuenta** con otra cuenta de Amazon | Vincula | ☐ |
| 15 | Añadir un producto desde cada una | Ambas confirman | ☐ |
| 16 | Firestore y las dos apps | **Cada producto en su lista. Ninguna cuenta ve el de la otra** | ☐ |

## Bloque 5 — Lista y caducidad

| # | Paso | Esperado | OK |
|---|---|---|---|
| 17 | En la app: MisListas → sección Alexa → elegir una segunda lista | Se marca como activa | ☐ |
| 18 | *"añade huevos"* | Va a la **nueva** lista | ☐ |
| 19 | Quitar `listaAlexa` a mano en Firestore y dictar otro producto | Cae en `listas[0]`, la predeterminada | ☐ |
| 20 | Esperar a que caduque el access token (>1 h) y volver a dictar | Funciona sin intervención: Alexa refresca sola | ☐ |

> El paso 20 tarda una hora. Déjalo lanzado y sigue con lo demás.

## Bloque 6 — La app, en los dos sistemas

| # | Paso | Esperado | OK |
|---|---|---|---|
| 21 | **Android**: MisListas con la sección Alexa, con cuenta vinculada | Selector visible, lista activa marcada | ☐ |
| 22 | **Android**: la misma pantalla en sesión anónima | Aviso, sin selector, y el enlace lleva al diálogo de vincular cuenta | ☐ |
| 23 | **Android**: cuenta sin vincular | Explica que se vincula desde la app de Alexa | ☐ |
| 24 | **Android**: usuario **sin ninguna lista** | Se ve la sección de Alexa **y** el mensaje de "no tienes listas" | ☐ |
| 25 | **iOS**: repetir 21, 17 y 18 | Igual que en Android | ☐ |
| 26 | Los tres idiomas: español, catalán e inglés | Ningún texto sin traducir en la sección Alexa | ☐ |

> El paso 24 es el que más vigilancia pide: la fase 5 **cambió** cómo se ve la pantalla sin
> listas. Antes era un mensaje centrado a pantalla completa; ahora la sección de Alexa va
> primero y el mensaje queda dentro del `LazyColumn`.

---

## Si algo falla

- **La skill responde 401 a todo** → mira los logs de Vercel. Si dice `ALEXA_SKILL_ID no
  está configurada`, falta la variable en ese entorno. Si dice `ALEXA_JWT_SECRET no está
  configurada`, lo mismo. Cualquier otra cosa es fallo de firma real.
- **Vincula pero la skill sigue pidiendo vincular** → el access token no verifica. Casi
  seguro que `ALEXA_JWT_SECRET` no es la misma con la que se firmó, o falta.
- **Vincula y dicta bien pero no aparece en la app** → mira a qué `listaId` fue en los logs
  (`Producto "X" guardado en lista Y`) y compáralo con `usuarios/{uid}.listaAlexa` y
  `listas[0]`.
- **Todo va bien pero llega tarde** → el endpoint de token tiene 4,5 s de límite y el de la
  skill 8 s. Medidos: 1,85 s en tibio y 0,47 s en caliente. En frío no está medido.
