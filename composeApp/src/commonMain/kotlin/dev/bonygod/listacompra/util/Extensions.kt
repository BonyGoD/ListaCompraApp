package dev.bonygod.listacompra.util

fun Boolean?.orTrue(): Boolean {
    return this != false
}

fun Boolean?.orFalse(): Boolean {
    return this == true
}

fun String?.orEmpty(): String {
    return this ?: ""
}

fun Long?.orZero(): Long {
    return this ?: 0L
}

fun Int?.orZero(): Int {
    return this ?: 0
}

fun String.dateFormat(vararg args: Any): String {
    var result = this
    var argIndex = 0

    val regex = Regex("%0?(\\d+)?d")
    result = regex.replace(result) { matchResult ->
        if (argIndex < args.size) {
            val width = matchResult.groupValues[1].toIntOrNull() ?: 0
            val value = args[argIndex++].toString()
            if (width > 0) {
                value.padStart(width, '0')
            } else {
                value
            }
        } else {
            matchResult.value
        }
    }

    return result
}

fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(this)
}