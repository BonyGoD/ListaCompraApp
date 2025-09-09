package dev.bonygod.listacompra.util

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val DATE_TIME_FORMAT_PATTERN = "%04d-%02d-%02d %02d:%02d"

private fun formatLocalDateTime(localDateTime: kotlinx.datetime.LocalDateTime): String {
    return DATE_TIME_FORMAT_PATTERN.dateFormat(
        localDateTime.year,
        localDateTime.monthNumber,
        localDateTime.dayOfMonth,
        localDateTime.hour,
        localDateTime.minute
    )
}

fun Timestamp?.timeStampTransform(): String {
    val instant = Instant.fromEpochSeconds(
        this?.seconds.orZero(),
        this?.nanoseconds.orZero()
    )
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return formatLocalDateTime(localDateTime)
}
