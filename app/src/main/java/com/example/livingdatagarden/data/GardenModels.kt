package com.example.livingdatagarden.data

import java.util.Date

/*
 * Shared contract between the data layer and the UI.
 * The UI only ever sees these types — never raw API JSON.
 */

/** The one fixed place this garden listens to (see README: not arbitrary user locations). */
data class GardenLocation(
    val city: String,
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        val MexicoCity = GardenLocation("Mexico City", 19.4326, -99.1332)
    }
}

/** The three illustrated states every flower can be in. */
enum class FlowerState(val label: String) {
    WILTED("Wilted"),
    STRESSED("Stressed"),
    BLOOMING("Blooming"),
}

enum class FlowerKind(val displayName: String) {
    AIR("Air flower"),
    WATER("Water flower"),
    ENERGY("Energy flower"),
}

data class FlowerReading(
    val kind: FlowerKind,
    /** Already formatted for display, e.g. "72", "18%". */
    val valueText: String,
    /** Raw value in source units (US AQI, m³/m³, W/m²). */
    val rawValue: Double,
    val state: FlowerState,
    /** 0f..1f, higher = happier. Thirds line up with WILTED / STRESSED / BLOOMING. */
    val health: Float,
    /** Last ~24 h of health values (0f..1f, oldest first). May be empty. */
    val series: List<Float>,
)

data class GardenSnapshot(
    val location: GardenLocation,
    val air: FlowerReading,
    val water: FlowerReading,
    val energy: FlowerReading,
    val temperatureC: Double,
    val elevationM: Double,
    /** IANA timezone of the location, e.g. "America/Mexico_City". */
    val timezone: String,
    val fetchedAt: Date,
) {
    val flowers: List<FlowerReading> get() = listOf(air, water, energy)

    /** Average flower health, 0f..1f. */
    val gardenHealth: Float get() = flowers.map { it.health }.average().toFloat()
}

sealed interface GardenResult {
    data class Success(val snapshot: GardenSnapshot) : GardenResult
    data class Failure(val reason: String) : GardenResult
}

interface GardenRepository {
    /** Never throws (except CancellationException); network/parse errors come back as Failure. */
    suspend fun fetch(location: GardenLocation = GardenLocation.MexicoCity): GardenResult
}
