package dev.bonygod.listacompra.core.review

/**
 * Pide al sistema que enseñe la valoración nativa. El sistema decide si se muestra y no
 * informa de ello: nadie debe depender del resultado. Nunca lanza.
 */
expect suspend fun requestAppReview()
