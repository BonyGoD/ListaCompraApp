package dev.bonygod.listacompra.util

fun Boolean?.orTrue(): Boolean { return this != false }
fun Boolean?.orFalse(): Boolean { return this == true }
fun String?.orEmpty(): String { return this ?: "" }
fun Long?.orZero(): Long { return this ?: 0L }
fun Int?.orZero(): Int { return this ?: 0 }