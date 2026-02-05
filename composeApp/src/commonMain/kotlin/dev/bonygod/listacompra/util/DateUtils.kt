package dev.bonygod.listacompra.util

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val DATE_TIME_FORMAT_PATTERN = "%04d-%02d-%02d %02d:%02d"

private fun formatLocalDateTime(localDateTime: LocalDateTime): String {
    return DATE_TIME_FORMAT_PATTERN.dateFormat(
        localDateTime.year,
        localDateTime.month,
        localDateTime.day,
        localDateTime.hour,
        localDateTime.minute
    )
}

@OptIn(ExperimentalTime::class)
fun Timestamp?.timeStampTransform(): String {
    val instant = Instant.fromEpochSeconds(
        this?.seconds.orZero(),
        this?.nanoseconds.orZero()
    )
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return formatLocalDateTime(localDateTime)
}
