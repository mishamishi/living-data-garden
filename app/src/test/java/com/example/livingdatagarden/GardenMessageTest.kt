package com.example.livingdatagarden

import com.example.livingdatagarden.data.FlowerKind
import com.example.livingdatagarden.data.FlowerReading
import com.example.livingdatagarden.data.FlowerState
import com.example.livingdatagarden.data.GardenLocation
import com.example.livingdatagarden.data.GardenSnapshot
import com.example.livingdatagarden.data.gardenMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class GardenMessageTest {
    private fun reading(kind: FlowerKind, state: FlowerState) = FlowerReading(
        kind, "1", 1.0, state,
        health = when (state) {
            FlowerState.WILTED -> 0.1f
            FlowerState.STRESSED -> 0.5f
            FlowerState.BLOOMING -> 0.9f
        },
        series = emptyList(),
    )

    private fun snap(a: FlowerState, w: FlowerState, e: FlowerState) = GardenSnapshot(
        GardenLocation.MexicoCity,
        reading(FlowerKind.AIR, a), reading(FlowerKind.WATER, w), reading(FlowerKind.ENERGY, e),
        20.0, 2200.0, "America/Mexico_City", Date(),
    )

    @Test fun allBloomingMatchesMockCopy() {
        val b = FlowerState.BLOOMING
        assertEquals(
            "Air’s quality is great today, so the air flower bloomed. Water and light are doing great as well.",
            gardenMessage(snap(b, b, b)),
        )
    }

    @Test fun worstFlowerLeads() {
        val m = gardenMessage(snap(FlowerState.BLOOMING, FlowerState.WILTED, FlowerState.STRESSED))
        assertTrue(m, m.startsWith("The soil is very dry"))
        assertTrue(m, m.contains("Air is doing great; light is a bit stressed."))
    }

    @Test fun allWilted() {
        val w = FlowerState.WILTED
        val m = gardenMessage(snap(w, w, w))
        assertTrue(m, m.startsWith("Air’s quality is poor"))
        assertTrue(m, m.endsWith("Water and light are struggling too."))
    }

    @Test fun allCombosFitTheBubble() {
        val states = FlowerState.values()
        for (a in states) for (w in states) for (e in states) {
            val m = gardenMessage(snap(a, w, e))
            assertTrue("$a/$w/$e: ${m.length} '$m'", m.length <= 120 && m.isNotBlank())
        }
    }
}
