package com.example.livingdatagarden.data

import kotlin.math.roundToInt

/*
 * Pure rules: raw environmental numbers -> flower state + health (0..1).
 * State thresholds and health thirds line up:
 *   health < 1/3  -> WILTED, 1/3 <= health < 2/3 -> STRESSED, >= 2/3 -> BLOOMING
 * (the AQI boundaries 50 and 100 are inclusive on the "better" side).
 */
object FlowerRules {
    private const val THIRD = 1f / 3f
    private const val TWO_THIRDS = 2f / 3f

    // (value, health) anchor points, value ascending. Health is linear between them.
    private val airCurve = listOf(0.0 to 1f, 50.0 to TWO_THIRDS, 100.0 to THIRD, 200.0 to 0f)
    private val waterCurve = listOf(0.0 to 0f, 0.10 to THIRD, 0.20 to TWO_THIRDS, 0.40 to 1f)
    private val lightCurve = listOf(0.0 to 0f, 200.0 to THIRD, 500.0 to TWO_THIRDS, 1000.0 to 1f)

    private fun valid(v: Double?): Boolean = v != null && v.isFinite() && v >= 0.0

    private fun curve(points: List<Pair<Double, Float>>, v: Double): Float {
        if (v <= points.first().first) return points.first().second
        if (v >= points.last().first) return points.last().second
        for (i in 1 until points.size) {
            val (x1, y1) = points[i]
            if (v <= x1) {
                val (x0, y0) = points[i - 1]
                val t = ((v - x0) / (x1 - x0)).toFloat()
                return (y0 + (y1 - y0) * t).coerceIn(0f, 1f)
            }
        }
        return points.last().second
    }

    // ---- Health -------------------------------------------------------

    fun airHealth(aqi: Double): Float = curve(airCurve, aqi)
    fun waterHealth(soilMoisture: Double): Float = curve(waterCurve, soilMoisture)
    fun lightHealth(radiation: Double): Float = curve(lightCurve, radiation)

    // ---- State --------------------------------------------------------

    fun airState(aqi: Double): FlowerState = when {
        aqi <= 50.0 -> FlowerState.BLOOMING
        aqi <= 100.0 -> FlowerState.STRESSED
        else -> FlowerState.WILTED
    }

    fun waterState(soilMoisture: Double): FlowerState = when {
        soilMoisture < 0.10 -> FlowerState.WILTED
        soilMoisture < 0.20 -> FlowerState.STRESSED
        else -> FlowerState.BLOOMING
    }

    fun lightState(radiation: Double): FlowerState = when {
        radiation < 200.0 -> FlowerState.WILTED
        radiation < 500.0 -> FlowerState.STRESSED
        else -> FlowerState.BLOOMING
    }

    // ---- Readings (null = current value unusable -> caller reports Failure) ----

    /** [hourly] = last-24h US AQI values, oldest first; invalid/null entries are skipped. */
    fun airReading(current: Double?, hourly: List<Double?>): FlowerReading? {
        if (!valid(current)) return null
        val aqi = current!!
        return FlowerReading(
            kind = FlowerKind.AIR,
            valueText = aqi.roundToInt().toString(),
            rawValue = aqi,
            state = airState(aqi),
            health = airHealth(aqi),
            series = hourly.filter(::valid).map { airHealth(it!!) },
        )
    }

    /** [hourly] = last-24h soil moisture (m3/m3), oldest first. */
    fun waterReading(current: Double?, hourly: List<Double?>): FlowerReading? {
        if (!valid(current)) return null
        val v = current!!
        return FlowerReading(
            kind = FlowerKind.WATER,
            valueText = "${(v * 100).roundToInt()}%",
            rawValue = v,
            state = waterState(v),
            health = waterHealth(v),
            series = hourly.filter(::valid).map { waterHealth(it!!) },
        )
    }

    /**
     * Night rule: when [isDay] is false, state and health come from the strongest radiation
     * of the last 24 h, so the light flower shows how sunny the day was instead of wilting
     * every night. valueText and rawValue still show the current reading.
     */
    fun energyReading(current: Double?, isDay: Boolean, hourly: List<Double?>): FlowerReading? {
        if (!valid(current)) return null
        val now = current!!
        val validHourly = hourly.filter(::valid).map { it!! }
        val effective = if (isDay) now else maxOf(now, validHourly.maxOrNull() ?: now)
        val percent = (now / 10.0).roundToInt().coerceIn(0, 100)
        return FlowerReading(
            kind = FlowerKind.ENERGY,
            valueText = "$percent%",
            rawValue = now,
            state = lightState(effective),
            health = lightHealth(effective),
            series = validHourly.map { lightHealth(it) },
        )
    }
}
