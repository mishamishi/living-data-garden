package com.example.livingdatagarden

import com.example.livingdatagarden.data.FlowerKind
import com.example.livingdatagarden.data.FlowerReading
import com.example.livingdatagarden.data.FlowerState
import com.example.livingdatagarden.data.GardenLocation
import com.example.livingdatagarden.data.GardenSnapshot
import java.util.Date

/** Fake data so @Preview functions keep working without the network. */
val previewSnapshot = GardenSnapshot(
    location = GardenLocation.MexicoCity,
    air = FlowerReading(
        FlowerKind.AIR, "72", 72.0, FlowerState.BLOOMING, 0.54f,
        listOf(0.25f, 0.30f, 0.38f, 0.50f, 0.55f, 0.50f, 0.42f, 0.40f, 0.48f, 0.55f)
    ),
    water = FlowerReading(
        FlowerKind.WATER, "78%", 0.78, FlowerState.STRESSED, 0.5f,
        listOf(0.40f, 0.45f, 0.35f, 0.28f, 0.25f, 0.30f, 0.55f, 0.70f, 0.75f, 0.78f)
    ),
    energy = FlowerReading(
        FlowerKind.ENERGY, "82%", 820.0, FlowerState.BLOOMING, 0.82f,
        listOf(0.85f, 0.70f, 0.45f, 0.25f, 0.15f, 0.15f, 0.25f, 0.45f, 0.70f, 0.82f)
    ),
    temperatureC = 28.0,
    elevationM = 2240.0,
    timezone = "America/Mexico_City",
    fetchedAt = Date(),
)

val previewState = GardenUiState(snapshot = previewSnapshot)
