package com.example.livingdatagarden

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val GardenFont = FontFamily(
    Font(R.font.cs_sullivan_pixel, FontWeight.Normal)
)

object AppSettings {
    var useFahrenheit by mutableStateOf(false)
    var use24h by mutableStateOf(false)
    var longDate by mutableStateOf(false)
    var lastUpdated by mutableStateOf(Date())
}

@Composable
fun rememberNow(): State<Date> = produceState(initialValue = Date()) {
    while (true) {
        delay(5000)
        value = Date()
    }
}

fun formatTemp(celsius: Int): String =
    if (AppSettings.useFahrenheit) "${celsius * 9 / 5 + 32}°F" else "$celsius°C"

fun clockMain(d: Date): String =
    SimpleDateFormat(if (AppSettings.use24h) "HH:mm" else "h:mm", Locale.ENGLISH).format(d)

fun clockSuffix(d: Date): String =
    if (AppSettings.use24h) "" else SimpleDateFormat("a", Locale.ENGLISH).format(d).uppercase()

fun dateLabel(d: Date): String =
    SimpleDateFormat(
        if (AppSettings.longDate) "EEEE d MMMM yyyy" else "EEE d MMM,",
        Locale.ENGLISH
    ).format(d)

fun updatedLabel(): String {
    val d = AppSettings.lastUpdated
    return "Updated " + clockMain(d) + (if (AppSettings.use24h) "" else " " + clockSuffix(d))
}