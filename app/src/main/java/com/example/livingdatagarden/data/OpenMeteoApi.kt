package com.example.livingdatagarden.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/*
 * Open-Meteo: free, no API key, so there are no secrets in the APK.
 * Everything numeric is nullable because the API sends null for missing hours.
 */

// ---- DTOs ---------------------------------------------------------------

@Serializable
data class AirResponse(
    val elevation: Double? = null,
    val timezone: String? = null,
    val current: AirCurrent? = null,
    val hourly: AirHourly? = null,
)

@Serializable
data class AirCurrent(
    val time: String? = null,
    @SerialName("us_aqi") val usAqi: Double? = null,
)

@Serializable
data class AirHourly(
    val time: List<String> = emptyList(),
    @SerialName("us_aqi") val usAqi: List<Double?> = emptyList(),
)

@Serializable
data class WeatherResponse(
    val elevation: Double? = null,
    val timezone: String? = null,
    val current: WeatherCurrent? = null,
    val hourly: WeatherHourly? = null,
)

@Serializable
data class WeatherCurrent(
    val time: String? = null,
    @SerialName("temperature_2m") val temperatureC: Double? = null,
    @SerialName("is_day") val isDay: Int? = null,
    @SerialName("shortwave_radiation") val shortwaveRadiation: Double? = null,
    @SerialName("soil_moisture_0_to_1cm") val soilMoisture: Double? = null,
)

@Serializable
data class WeatherHourly(
    val time: List<String> = emptyList(),
    @SerialName("soil_moisture_0_to_1cm") val soilMoisture: List<Double?> = emptyList(),
    @SerialName("shortwave_radiation") val shortwaveRadiation: List<Double?> = emptyList(),
)

// ---- HTTP ---------------------------------------------------------------

/** Fetches a URL and returns the body. Swap it out in tests to use canned JSON. */
fun interface HttpGetter {
    suspend fun get(url: String): String
}

private const val MAX_BODY_BYTES = 512 * 1024
private const val TIMEOUT_MS = 10_000

class HttpUrlConnectionGetter : HttpGetter {
    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.useCaches = false
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw IOException("HTTP $code")

            connection.inputStream.use { input ->
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    if (out.size() + n > MAX_BODY_BYTES) throw IOException("Response too large")
                    out.write(buffer, 0, n)
                }
                out.toString(Charsets.UTF_8.name())
            }
        } finally {
            connection.disconnect()
        }
    }
}

// ---- URLs + parsing -----------------------------------------------------

object OpenMeteoApi {
    const val AIR_BASE_URL = "https://air-quality-api.open-meteo.com/v1/air-quality"
    const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/forecast"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    fun isValid(latitude: Double, longitude: Double): Boolean =
        latitude in -90.0..90.0 && longitude in -180.0..180.0

    fun airUrl(latitude: Double, longitude: Double): String =
        "$AIR_BASE_URL?${coords(latitude, longitude)}" +
            "&current=us_aqi&hourly=us_aqi&past_days=1&forecast_days=1&timezone=auto"

    fun weatherUrl(latitude: Double, longitude: Double): String =
        "$WEATHER_BASE_URL?${coords(latitude, longitude)}" +
            "&current=temperature_2m,is_day,shortwave_radiation,soil_moisture_0_to_1cm" +
            "&hourly=soil_moisture_0_to_1cm,shortwave_radiation" +
            "&past_days=1&forecast_days=1&timezone=auto"

    private fun coords(latitude: Double, longitude: Double): String {
        require(isValid(latitude, longitude)) { "Invalid coordinates" }
        // Locale.US so we never send "19,4326".
        return String.format(Locale.US, "latitude=%.4f&longitude=%.4f", latitude, longitude)
    }

    fun parseAir(body: String): AirResponse = json.decodeFromString(body)

    fun parseWeather(body: String): WeatherResponse = json.decodeFromString(body)
}
