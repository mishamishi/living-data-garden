package com.example.livingdatagarden

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date

private val InkColor = Color(0xFF1A1A1A)
private val MutedColor = Color(0xFF6B6B6B)

private data class FlowerStat(
    val flowerRes: Int,
    val value: String,
    val label: String,
    val status: String,
    val color: Color,
    val chartColor: Color,
    val fill: Float,
    val series: List<Float>,
    val name: String
)

@Composable
private fun StatusBar(fill: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.height(14.dp)) {
        val r = size.height / 2
        drawRoundRect(Color(0xFFE2E2E2), cornerRadius = CornerRadius(r, r))
        drawRoundRect(color, size = Size(size.width * fill, size.height), cornerRadius = CornerRadius(r, r))
        listOf(0.33f, 0.67f).forEach { t ->
            val x = size.width * t
            drawLine(
                Color.Black,
                Offset(x, -2.dp.toPx()),
                Offset(x, size.height + 2.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
private fun SparkChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.size >= 2) {
            val stepX = size.width / (values.size - 1)
            val pts = values.mapIndexed { i, v -> Offset(i * stepX, size.height * (1f - v)) }
            val line = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                pts.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val area = Path().apply {
                addPath(line)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(area, color.copy(alpha = 0.18f))
            drawPath(
                line,
                color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawCircle(Color.Black, 5.dp.toPx(), pts.last())
        }
    }
}

@Composable
private fun FlowerCard(stat: FlowerStat) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
    ) {
        TexturedBox(R.drawable.bg_dash_card, Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(stat.flowerRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(72.dp).fillMaxHeight()
                )
                Spacer(Modifier.width(10.dp))
                Column(
                    Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stat.value, fontFamily = GardenFont, fontSize = 28.sp, color = InkColor)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stat.label,
                            fontFamily = GardenFont,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            color = MutedColor,
                            modifier = Modifier.weight(1f)
                        )
                        Column(Modifier.width(92.dp), horizontalAlignment = Alignment.End) {
                            Text(stat.status, fontFamily = GardenFont, fontSize = 11.sp, color = stat.color)
                            Spacer(Modifier.height(2.dp))
                            StatusBar(stat.fill, stat.color, Modifier.fillMaxWidth())
                        }
                    }
                    Text("Last 24 hours", fontFamily = GardenFont, fontSize = 10.sp, color = MutedColor)
                    SparkChart(stat.series, stat.chartColor, Modifier.fillMaxWidth().height(34.dp))
                    Box(Modifier.fillMaxWidth()) {
                        Text(
                            "24h ago",
                            fontFamily = GardenFont,
                            fontSize = 10.sp,
                            color = MutedColor,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                        Text(
                            stat.name,
                            fontFamily = GardenFont,
                            fontSize = 15.sp,
                            color = InkColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Text(
                            "now",
                            fontFamily = GardenFont,
                            fontSize = 10.sp,
                            color = MutedColor,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-14).dp)
                .size(36.dp)
                .background(Color(0xFFD4E89C), shape = CircleShape)
                .drawBehind {
                    drawCircle(
                        color = InkColor,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "i",
                fontFamily = GardenFont,
                fontSize = 18.sp,
                color = InkColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HealthCard() {
    TexturedBox(R.drawable.bg_dash_health, Modifier.fillMaxWidth().height(66.dp)) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Garden’s\nHealth",
                fontFamily = GardenFont,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.End,
                color = InkColor
            )
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Wilted", fontFamily = GardenFont, fontSize = 12.sp, color = InkColor)
                    Text("Blooming", fontFamily = GardenFont, fontSize = 12.sp, color = InkColor)
                }
                Spacer(Modifier.height(4.dp))
                StatusBar(0.6f, Color(0xFFD9F5A5), Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun DashboardScreen(onHome: () -> Unit) {
    val stats = listOf(
        FlowerStat(
            R.drawable.dash_flower_air, "72", "Air quality\nindex (AQI)", "Blooming",
            Color(0xFFFFB3C7), Color(0xFFC3A8F0), 0.54f,
            listOf(0.25f, 0.30f, 0.38f, 0.50f, 0.55f, 0.50f, 0.42f, 0.40f, 0.48f, 0.55f),
            "Air flower"
        ),
        FlowerStat(
            R.drawable.dash_flower_water, "78%", "Soil\nmoisture", "Blooming",
            Color(0xFF7CC4E8), Color(0xFF7CC4E8), 0.54f,
            listOf(0.40f, 0.45f, 0.35f, 0.28f, 0.25f, 0.30f, 0.55f, 0.70f, 0.75f, 0.78f),
            "Water flower"
        ),
        FlowerStat(
            R.drawable.dash_flower_energy, "82%", "Light\nlevel", "Blooming",
            Color(0xFFF2C14E), Color(0xFFF2C14E), 0.55f,
            listOf(0.85f, 0.70f, 0.45f, 0.25f, 0.15f, 0.15f, 0.25f, 0.45f, 0.70f, 0.82f),
            "Energy flower"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE1FDA1))))
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = updatedLabel(),
                    fontFamily = GardenFont,
                    fontSize = 16.sp,
                    color = InkColor,
                    modifier = Modifier
                        .clickable { AppSettings.lastUpdated = Date() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("Electricity data from Electricity Maps", fontFamily = GardenFont, fontSize = 7.sp, color = InkColor)
                Text("Water data from CONAGUA", fontFamily = GardenFont, fontSize = 7.sp, color = InkColor)
                Text("Air data from Open-Meteo", fontFamily = GardenFont, fontSize = 7.sp, color = InkColor)
            }

            Spacer(Modifier.height(4.dp))
            TexturedBox(R.drawable.bg_bubble, Modifier.fillMaxWidth().height(64.dp)) {
                Text(
                    text = "Air’s quality is great today, so the air flower bloomed. Water and light are doing great as well.",
                    fontFamily = GardenFont,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = InkColor,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(2.dp))
            stats.forEach { stat ->
                FlowerCard(stat)
            }
            HealthCard()
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_ldg),
                    contentDescription = "Living Data Garden",
                    modifier = Modifier.fillMaxWidth(0.95f),
                    contentScale = ContentScale.FillWidth
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .clickable(onClick = onHome),
                contentAlignment = Alignment.BottomEnd
            ) {
                TexturedBox(
                    R.drawable.bg_expand,
                    Modifier.fillMaxWidth().height(78.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 14.dp, top = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Hide", fontFamily = GardenFont, fontSize = 13.sp, color = MutedColor)
                        Text("Dashboard", fontFamily = GardenFont, fontSize = 22.sp, color = InkColor)
                    }
                }
                Image(
                    painter = painterResource(R.drawable.ic_hide_circle),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(54.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun DashboardPreview() {
    DashboardScreen(onHome = {})
}