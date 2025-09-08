package dev.bonygod.listacompra.util

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Timestamp?.timeStampTransform(): String {
    val instant = Instant.fromEpochSeconds(
        this?.seconds.orZero(),
        this?.nanoseconds.orZero()
    )
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val fechaString =
        "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')} " +
        "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    return fechaString
}