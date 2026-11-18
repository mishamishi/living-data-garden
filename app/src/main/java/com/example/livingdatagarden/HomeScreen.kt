package com.example.livingdatagarden

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.livingdatagarden.data.FlowerState
import com.example.livingdatagarden.data.GardenSnapshot
import com.example.livingdatagarden.data.gardenMessage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val Ink = Color(0xFF1A1A1A)
private val Muted = Color(0xFF6B6B6B)

private fun formatLatitude(lat: Double): String =
    String.format(Locale.US, "%.4f° %s", Math.abs(lat), if (lat < 0) "S" else "N")

private fun formatElevation(m: Double): String =
    String.format(Locale.US, "%,d m", Math.round(m))

@Composable
fun TexturedBox(
    @DrawableRes bg: Int,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = contentAlignment) {
        Image(
            painter = painterResource(bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        content()
    }
}

@Composable
fun GardenIllustration(snapshot: GardenSnapshot?, modifier: Modifier = Modifier) {
    Box(modifier) {
        listOf(
            R.drawable.green_bloom to FlowerState.BLOOMING,
            R.drawable.air_bloom to (snapshot?.air?.state ?: FlowerState.BLOOMING),
            R.drawable.water_bloom to (snapshot?.water?.state ?: FlowerState.BLOOMING),
            R.drawable.energy_bloom to (snapshot?.energy?.state ?: FlowerState.BLOOMING)
        ).forEach { (res, flowerState) ->
            Image(
                painter = painterResource(res),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = flowerStateFilter(flowerState),
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun Info(label: String, value: String) {
    Column {
        Text(label, fontFamily = GardenFont, fontSize = 11.sp, color = Muted)
        Text(value, fontFamily = GardenFont, fontSize = 17.sp, color = Ink)
    }
}

@Composable
private fun ToggleSegment(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Image(
                painter = painterResource(R.drawable.bg_toggle_selected),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )
        }
        Text(label, fontFamily = GardenFont, fontSize = 20.sp, color = Ink)
    }
}

@Composable
fun HomeScreen(
    state: GardenUiState,
    onDashboard: () -> Unit,
    onRefresh: () -> Unit,
    onSetLive: (Boolean) -> Unit
) {
    val now by rememberNow()
    val snapshot = state.snapshot
    val tz = gardenTimeZone(snapshot)
    val live = state.live

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE1FDA1))))
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = updatedLabel(snapshot?.fetchedAt, tz, state.isRefreshing, state.error != null),
            fontFamily = GardenFont,
            fontSize = 13.sp,
            color = Ink,
            modifier = Modifier
                .clickable(onClick = onRefresh)
                .padding(start = 12.dp, top = 4.dp, bottom = 8.dp, end = 12.dp)
        )

        TexturedBox(R.drawable.bg_bubble, Modifier.fillMaxWidth().height(84.dp)) {
            Text(
                text = when {
                    snapshot != null -> gardenMessage(snapshot)
                    state.error != null -> "The garden can’t hear the sky right now. Tap the time above to try again."
                    else -> "The garden is listening for today’s air, water and light…"
                },
                fontFamily = GardenFont,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = Ink,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        GardenIllustration(snapshot, Modifier.fillMaxWidth().height(220.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(R.drawable.logo_ldg),
                contentDescription = "Living Data Garden",
                modifier = Modifier.width(190.dp)
            )
            Box(
                Modifier
                    .width(150.dp)
                    .height(84.dp)
                    .clickable(onClick = onDashboard)
            ) {
                TexturedBox(
                    R.drawable.bg_expand,
                    Modifier.align(Alignment.BottomStart).fillMaxWidth().height(70.dp)
                ) {
                    Column(Modifier.padding(start = 14.dp)) {
                        Text("Expand", fontFamily = GardenFont, fontSize = 11.sp, color = Muted)
                        Text("Dashboard", fontFamily = GardenFont, fontSize = 19.sp, color = Ink)
                    }
                }
                Image(
                    painter = painterResource(R.drawable.ic_expand_circle),
                    contentDescription = "Expand dashboard",
                    modifier = Modifier.align(Alignment.TopEnd).size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        TexturedBox(R.drawable.bg_live_data, Modifier.fillMaxWidth().height(44.dp)) {
            Text(
                "Live Data",
                fontFamily = GardenFont,
                fontSize = 20.sp,
                color = Ink,
                modifier = Modifier.padding(start = 28.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TexturedBox(R.drawable.bg_time, Modifier.weight(0.85f).fillMaxHeight()) {
                Column(Modifier.padding(start = 16.dp)) {
                    Text(clockSuffix(now, tz), fontFamily = GardenFont, fontSize = 18.sp, color = Muted)
                    Text(clockMain(now, tz), fontFamily = GardenFont, fontSize = 42.sp, color = Ink)
                    Spacer(Modifier.height(12.dp))
                    Text(dateLabel(now, tz) + "\nlocal time", fontFamily = GardenFont, fontSize = 12.sp, color = Ink)
                }
            }
            Column(
                modifier = Modifier.weight(1.6f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TexturedBox(R.drawable.bg_city, Modifier.weight(1.2f).fillMaxHeight()) {
                        Box(Modifier.padding(start = 14.dp)) { Info("City", snapshot?.location?.city ?: "–") }
                    }
                    TexturedBox(R.drawable.bg_temp, Modifier.weight(1f).fillMaxHeight()) {
                        Box(Modifier.padding(start = 14.dp)) { Info("Temperature", snapshot?.let { formatTemp(Math.round(it.temperatureC).toInt()) } ?: "–") }
                    }
                }
                TexturedBox(R.drawable.bg_latlon, Modifier.fillMaxWidth().weight(1f)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Box(Modifier.weight(1f)) { Info("Latitude", snapshot?.let { formatLatitude(it.location.latitude) } ?: "–") }
                        Box(Modifier.weight(1f).padding(start = 12.dp)) { Info("Altitude", snapshot?.let { formatElevation(it.elevationM) } ?: "–") }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        TexturedBox(
            R.drawable.bg_toggle,
            Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp)
        ) {
            Row(Modifier.fillMaxSize().padding(6.dp)) {
                ToggleSegment("Live", live, Modifier.weight(1f)) { onSetLive(true) }
                ToggleSegment("Manual", !live, Modifier.weight(1f)) { onSetLive(false) }
            }
        }

        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Air data from Open-Meteo", fontFamily = GardenFont, fontSize = 8.sp, color = Ink)
            Text("Water data from Open-Meteo", fontFamily = GardenFont, fontSize = 8.sp, color = Ink)
            Text("Light data from Open-Meteo", fontFamily = GardenFont, fontSize = 8.sp, color = Ink)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomePreview() {
    HomeScreen(state = previewState, onDashboard = {}, onRefresh = {}, onSetLive = {})
}

