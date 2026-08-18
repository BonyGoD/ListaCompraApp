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

- [ ] 34a. **Repetir el paso 18**: vincular cuenta y, sin reiniciar, crear a mano la
      invitación en Firestore → **debe aparecer sola**
- [ ] 34b. Tras vincular, comprobar que **la lista y sus productos siguen bien**
      (se añadió una recarga de datos en ese camino)
- [ ] 34c. Con una cuenta **que no sea la del desarrollador**: los botones
      **"Forzar crash (test)" y "Forzar non-fatal (test)" NO aparecen**
- [ ] 34d. Con **bonygod.dev@gmail.com**: **sí aparecen** y siguen funcionando
- [ ] 34e. Móvil en inglés → **"Add product"**, **"Clear list"** y el diálogo de
      confirmación de borrado, traducidos

## Bloque 9 — iOS (en el Mac)

- [ ] 35. **Xcode compila y enlaza el framework** tras quitar `iosX64()`
- [ ] 36. Repetir bloque 2 (arranque)
- [ ] 37. Repetir bloque 3 (vincular)
- [ ] 38. Repetir bloque 6 (aceptar y rechazar)
- [ ] 39. Anotar qué se ve al arrancar → el *launch screen* de iOS sigue sin revisar

---

## Pendiente tras las pruebas

- [ ] Publicar la app
- [ ] Esperar a que se propague
- [ ] **Entonces** volver a endurecer las reglas de `notifications`

## Si algo falla

Logcat filtrado por el paquete, y **Crashlytics** para las excepciones: los
repositorios registran todas con `recordException` antes de devolver el fallo.
Los mensajes en pantalla son genéricos a propósito.
