package com.example.livingdatagarden

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.livingdatagarden.data.FlowerState
import com.example.livingdatagarden.data.GardenSnapshot
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val GardenFont = FontFamily(
    Font(R.font.cs_sullivan_pixel, FontWeight.Normal)
)

object AppSettings {
    var useFahrenheit by mutableStateOf(false)
    var use24h by mutableStateOf(false)
    var longDate by mutableStateOf(false)
}

@Composable
fun rememberNow(): State<Date> = produceState(initialValue = Date()) {
    while (true) {
        delay(5000)
        value = Date()
    }
}

/** The garden's own timezone (fixed location); device timezone until data arrives. */
fun gardenTimeZone(snapshot: GardenSnapshot?): TimeZone =
    snapshot?.let { TimeZone.getTimeZone(it.timezone) } ?: TimeZone.getDefault()

fun formatTemp(celsius: Int): String =
    if (AppSettings.useFahrenheit) "${celsius * 9 / 5 + 32}°F" else "$celsius°C"

fun clockMain(d: Date, tz: TimeZone = TimeZone.getDefault()): String =
    SimpleDateFormat(if (AppSettings.use24h) "HH:mm" else "h:mm", Locale.ENGLISH)
        .apply { timeZone = tz }.format(d)

fun clockSuffix(d: Date, tz: TimeZone = TimeZone.getDefault()): String =
    if (AppSettings.use24h) "" else SimpleDateFormat("a", Locale.ENGLISH)
        .apply { timeZone = tz }.format(d).uppercase()

fun dateLabel(d: Date, tz: TimeZone = TimeZone.getDefault()): String =
    SimpleDateFormat(
        if (AppSettings.longDate) "EEEE d MMMM yyyy" else "EEE d MMM,",
        Locale.ENGLISH
    ).apply { timeZone = tz }.format(d)

fun updatedLabel(
    updated: Date?,
    tz: TimeZone = TimeZone.getDefault(),
    isRefreshing: Boolean = false,
    offline: Boolean = false
): String = when {
    isRefreshing -> "Updating…"
    updated != null ->
        "Updated " + clockMain(updated, tz) + (if (AppSettings.use24h) "" else " " + clockSuffix(updated, tz))
    offline -> "Offline"
    else -> "Updating…"
}

/**
 * Placeholder visual for flower states until the author draws wilted/stressed illustrations.
 * BLOOMING = untouched, STRESSED = partly desaturated, WILTED = grey and faded.
 * Reused by the Home garden and the Dashboard flower icons.
 */
fun flowerStateFilter(state: FlowerState): ColorFilter? = when (state) {
    FlowerState.BLOOMING -> null
    FlowerState.STRESSED -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.45f) })
    FlowerState.WILTED -> ColorFilter.colorMatrix(ColorMatrix().apply {
        setToSaturation(0f)
        this[3, 3] = 0.6f
    })
}
