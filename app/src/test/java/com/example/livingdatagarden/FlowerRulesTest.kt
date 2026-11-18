package com.example.livingdatagarden

import com.example.livingdatagarden.data.FlowerRules
import com.example.livingdatagarden.data.FlowerState.BLOOMING
import com.example.livingdatagarden.data.FlowerState.STRESSED
import com.example.livingdatagarden.data.FlowerState.WILTED
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowerRulesTest {
    @Test fun airBoundaries() {
        assertEquals(BLOOMING, FlowerRules.airState(0.0))
        assertEquals(BLOOMING, FlowerRules.airState(50.0))
        assertEquals(STRESSED, FlowerRules.airState(51.0))
        assertEquals(STRESSED, FlowerRules.airState(100.0))
        assertEquals(WILTED, FlowerRules.airState(101.0))
        assertEquals(1f, FlowerRules.airHealth(0.0), 0.001f)
        assertEquals(0.667f, FlowerRules.airHealth(50.0), 0.001f)
        assertEquals(0.333f, FlowerRules.airHealth(100.0), 0.001f)
        assertEquals(0f, FlowerRules.airHealth(200.0), 0.001f)
        assertEquals(0f, FlowerRules.airHealth(500.0), 0.001f)
    }

    @Test fun waterBoundaries() {
        assertEquals(WILTED, FlowerRules.waterState(0.099))
        assertEquals(STRESSED, FlowerRules.waterState(0.10))
        assertEquals(STRESSED, FlowerRules.waterState(0.199))
        assertEquals(BLOOMING, FlowerRules.waterState(0.20))
        assertEquals(0f, FlowerRules.waterHealth(0.0), 0.001f)
        assertEquals(0.333f, FlowerRules.waterHealth(0.10), 0.001f)
        assertEquals(0.667f, FlowerRules.waterHealth(0.20), 0.001f)
        assertEquals(1f, FlowerRules.waterHealth(0.9), 0.001f)
    }

    @Test fun lightBoundaries() {
        assertEquals(WILTED, FlowerRules.lightState(199.0))
        assertEquals(STRESSED, FlowerRules.lightState(200.0))
        assertEquals(STRESSED, FlowerRules.lightState(499.0))
        assertEquals(BLOOMING, FlowerRules.lightState(500.0))
        assertEquals(0.333f, FlowerRules.lightHealth(200.0), 0.001f)
        assertEquals(0.667f, FlowerRules.lightHealth(500.0), 0.001f)
        assertEquals(1f, FlowerRules.lightHealth(1500.0), 0.001f)
    }

    @Test fun stateAndHealthAgreeAroundBoundaries() {
        fun third(h: Float) = when {
            h >= 2f / 3f -> BLOOMING
            h >= 1f / 3f -> STRESSED
            else -> WILTED
        }
        for (aqi in listOf(0.0, 25.0, 50.0, 50.5, 75.0, 100.0, 100.5, 150.0, 300.0))
            assertEquals("aqi $aqi", FlowerRules.airState(aqi), third(FlowerRules.airHealth(aqi)))
        for (m in listOf(0.0, 0.05, 0.0999, 0.10, 0.15, 0.1999, 0.20, 0.3, 0.5))
            assertEquals("soil $m", FlowerRules.waterState(m), third(FlowerRules.waterHealth(m)))
        for (r in listOf(0.0, 100.0, 199.9, 200.0, 350.0, 499.9, 500.0, 800.0, 1200.0))
            assertEquals("rad $r", FlowerRules.lightState(r), third(FlowerRules.lightHealth(r)))
    }

    @Test fun valueTexts() {
        assertEquals("103", FlowerRules.airReading(103.2, emptyList())!!.valueText)
        assertEquals("10%", FlowerRules.waterReading(0.102, emptyList())!!.valueText)
        assertEquals("82%", FlowerRules.energyReading(820.0, true, emptyList())!!.valueText)
        assertEquals("100%", FlowerRules.energyReading(1400.0, true, emptyList())!!.valueText)
    }

    @Test fun nightUsesMaxOfLast24h() {
        val hourly = listOf(0.0, 650.0, null, 0.0)
        val night = FlowerRules.energyReading(0.0, false, hourly)!!
        assertEquals(BLOOMING, night.state)
        assertEquals(FlowerRules.lightHealth(650.0), night.health, 0.001f)
        assertEquals("0%", night.valueText)
        assertEquals(0.0, night.rawValue, 0.0)
        // Same data in daytime: the current value rules.
        assertEquals(WILTED, FlowerRules.energyReading(0.0, true, hourly)!!.state)
    }

    @Test fun invalidCurrentValuesFail() {
        assertNull(FlowerRules.airReading(null, emptyList()))
        assertNull(FlowerRules.airReading(Double.NaN, emptyList()))
        assertNull(FlowerRules.waterReading(-0.1, emptyList()))
        assertNull(FlowerRules.energyReading(Double.POSITIVE_INFINITY, true, emptyList()))
    }

    @Test fun invalidSeriesValuesAreSkipped() {
        val r = FlowerRules.airReading(40.0, listOf(10.0, null, Double.NaN, -5.0, 120.0))
        assertNotNull(r)
        assertEquals(2, r!!.series.size)
        assertTrue(r.series.all { it in 0f..1f })
    }
}
