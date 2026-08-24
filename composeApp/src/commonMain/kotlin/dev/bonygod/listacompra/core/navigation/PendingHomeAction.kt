package dev.bonygod.listacompra.core.navigation

/**
 * Peticiones de un solo uso que una pantalla deja hechas para Home antes de navegar a ella.
 *
 * Esto vivía como `Routes.Home(userId, openLinkAccount = true)`, y dio dos bugs seguidos
 * el 23 ago 2026, los dos por lo mismo: **una ruta no es un evento**. El flag se quedaba
 * grabado en la entrada del backstack y no se limpiaba nunca, así que el diálogo reaparecía
 * al volver a Home desde cualquier sitio. Y al meter un guard con `rememberSaveable` salió
 * el problema opuesto: como `Routes.Home` es un data class, volver a navegar con el mismo
 * flag produce una entrada **igual** a la anterior, se restaura su estado guardado, el guard
 * seguía consumido y entonces el diálogo ya no salía nunca más.
 *
 * Aquí la petición se consume de verdad: `consumeLinkAccount()` devuelve `true` una sola vez
 * y se apaga sola. No hay nada que limpiar a mano ni estado que sobreviva a la navegación.
 */
class PendingHomeAction {

    private var linkAccountRequested = false

    /** La pantalla de Alexa pide que Home abra el diálogo de crear cuenta al llegar. */
    fun requestLinkAccount() {
        linkAccountRequested = true
    }

    /** Devuelve `true` como mucho una vez por petición. */
    fun consumeLinkAccount(): Boolean {
        val requested = linkAccountRequested
        linkAccountRequested = false
        return requested
    }
}
