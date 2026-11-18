package com.example.livingdatagarden

import com.example.livingdatagarden.data.FlowerState
import com.example.livingdatagarden.data.GardenResult
import com.example.livingdatagarden.data.HttpGetter
import com.example.livingdatagarden.data.OpenMeteoGardenRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class OpenMeteoGardenRepositoryTest {
    private fun airJson(aqiHourly: String = "[40,60,null,103,110]") = """
        {"elevation":2230.0,"timezone":"America/Mexico_City","unknown":1,
         "current":{"time":"2026-11-16T02:00","interval":3600,"us_aqi":103},
         "hourly":{"time":["2026-11-16T00:00","2026-11-16T01:00","2026-11-16T02:00","2026-11-16T03:00","2026-11-16T04:00"],
                   "us_aqi":$aqiHourly}}
    """.trimIndent()

    private fun weatherJson(isDay: Int = 0, soil: String = "0.102") = """
        {"elevation":2230.0,"timezone":"America/Mexico_City",
         "current":{"time":"2026-11-16T02:15","interval":900,"temperature_2m":13.3,"is_day":$isDay,
                    "shortwave_radiation":0.0,"soil_moisture_0_to_1cm":$soil},
         "hourly":{"time":["2026-11-15T12:00","2026-11-15T13:00","2026-11-16T02:00","2026-11-16T12:00"],
                   "soil_moisture_0_to_1cm":[0.1,null,0.102,0.3],
                   "shortwave_radiation":[800.0,null,0.0,900.0]}}
    """.trimIndent()

    private fun fake(air: String, weather: String) = HttpGetter { url ->
        if (url.startsWith("https://air-quality-api.open-meteo.com/")) air else weather
    }

    @Test fun success() = runTest {
        val r = OpenMeteoGardenRepository(fake(airJson(), weatherJson())).fetch()
        val s = (r as GardenResult.Success).snapshot
        assertEquals("103", s.air.valueText)
        assertEquals(FlowerState.WILTED, s.air.state)
        assertEquals(2, s.air.series.size) // 40, 60 kept; null skipped; future hours dropped
        assertEquals("10%", s.water.valueText)
        assertEquals(FlowerState.STRESSED, s.water.state)
        assertEquals(2, s.water.series.size)
        // Night: light flower reflects yesterday's 800 W/m2 peak.
        assertEquals(FlowerState.BLOOMING, s.energy.state)
        assertEquals("0%", s.energy.valueText)
        assertEquals(13.3, s.temperatureC, 0.0)
        assertEquals(2230.0, s.elevationM, 0.0)
        assertEquals("America/Mexico_City", s.timezone)
    }

    @Test fun daytimeUsesCurrentRadiation() = runTest {
        val r = OpenMeteoGardenRepository(fake(airJson(), weatherJson(isDay = 1))).fetch()
        assertEquals(FlowerState.WILTED, (r as GardenResult.Success).snapshot.energy.state)
    }

    @Test fun requestsUseDotDecimalsAndHttps() = runTest {
        val urls = mutableListOf<String>()
        val http = HttpGetter { url ->
            urls += url
            if ("air-quality" in url) airJson() else weatherJson()
        }
        OpenMeteoGardenRepository(http).fetch()
        assertEquals(2, urls.size)
        assertTrue(urls.all { it.startsWith("https://") && "latitude=19.4326" in it && "longitude=-99.1332" in it })
    }

    @Test fun ioFailureBecomesSafeFailure() = runTest {
        val http = HttpGetter { throw IOException("secret https://x?key=abc") }
        val r = OpenMeteoGardenRepository(http).fetch() as GardenResult.Failure
        assertEquals("Couldn’t reach Open-Meteo", r.reason)
        assertFalse(r.reason.contains("secret"))
    }

    @Test fun malformedJsonBecomesFailure() = runTest {
        val r = OpenMeteoGardenRepository(fake("not json", weatherJson())).fetch()
        assertEquals("Unexpected data from Open-Meteo", (r as GardenResult.Failure).reason)
    }

    @Test fun missingCurrentValueBecomesFailure() = runTest {
        val r = OpenMeteoGardenRepository(fake(airJson(), weatherJson(soil = "null"))).fetch()
        assertTrue(r is GardenResult.Failure)
    }
}
