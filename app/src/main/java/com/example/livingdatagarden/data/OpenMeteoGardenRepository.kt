package com.example.livingdatagarden.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.IOException
import java.util.Date

class OpenMeteoGardenRepository(
    private val http: HttpGetter = HttpUrlConnectionGetter(),
) : GardenRepository {

    override suspend fun fetch(location: GardenLocation): GardenResult {
        if (!OpenMeteoApi.isValid(location.latitude, location.longitude)) {
            return GardenResult.Failure("Invalid garden location")
        }
        return try {
            val (airBody, weatherBody) = coroutineScope {
                val air = async { http.get(OpenMeteoApi.airUrl(location.latitude, location.longitude)) }
                val weather = async { http.get(OpenMeteoApi.weatherUrl(location.latitude, location.longitude)) }
                air.await() to weather.await()
            }
            val snapshot = buildSnapshot(
                location,
                OpenMeteoApi.parseAir(airBody),
                OpenMeteoApi.parseWeather(weatherBody),
            ) ?: return GardenResult.Failure(UNEXPECTED)
            GardenResult.Success(snapshot)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            GardenResult.Failure("Couldn’t reach Open-Meteo")
        } catch (e: Exception) {
            // Includes SerializationException. Never expose raw messages or URLs.
            GardenResult.Failure(UNEXPECTED)
        }
    }

    private fun buildSnapshot(
        location: GardenLocation,
        airResponse: AirResponse,
        weatherResponse: WeatherResponse,
    ): GardenSnapshot? {
        val airNow = airResponse.current ?: return null
        val weatherNow = weatherResponse.current ?: return null
        val temperature = weatherNow.temperatureC?.takeIf { it.isFinite() } ?: return null

        val airHourly = airResponse.hourly
        val airSeries = last24(airNow.time, airHourly?.time.orEmpty(), airHourly?.usAqi.orEmpty())
        val wHourly = weatherResponse.hourly
        val soilSeries = last24(weatherNow.time, wHourly?.time.orEmpty(), wHourly?.soilMoisture.orEmpty())
        val lightSeries = last24(weatherNow.time, wHourly?.time.orEmpty(), wHourly?.shortwaveRadiation.orEmpty())

        val air = FlowerRules.airReading(airNow.usAqi, airSeries) ?: return null
        val water = FlowerRules.waterReading(weatherNow.soilMoisture, soilSeries) ?: return null
        // A missing is_day is treated as daytime (no night rule).
        val energy = FlowerRules.energyReading(
            weatherNow.shortwaveRadiation,
            isDay = weatherNow.isDay != 0,
            hourly = lightSeries,
        ) ?: return null

        return GardenSnapshot(
            location = location,
            air = air,
            water = water,
            energy = energy,
            temperatureC = temperature,
            elevationM = weatherResponse.elevation ?: airResponse.elevation ?: 0.0,
            timezone = weatherResponse.timezone ?: airResponse.timezone ?: "UTC",
            fetchedAt = Date(),
        )
    }

    /**
     * Hourly entries up to and including "now", last 24 of them, oldest first.
     * ISO local times (same tz + format) compare correctly as plain strings.
     */
    private fun last24(now: String?, times: List<String>, values: List<Double?>): List<Double?> {
        if (now == null) return emptyList()
        return times.zip(values)
            .filter { (time, _) -> time <= now }
            .takeLast(24)
            .map { it.second }
    }

    private companion object {
        const val UNEXPECTED = "Unexpected data from Open-Meteo"
    }
}
