package com.example.livingdatagarden

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RuledText(text: String, modifier: Modifier = Modifier) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        fontFamily = GardenFont,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        color = Color(0xFF1A1A1A),
        onTextLayout = { layout = it },
        modifier = modifier.drawBehind {
            layout?.let { l ->
                for (i in 0 until l.lineCount) {
                    val y = l.getLineBottom(i) - 3.dp.toPx()
                    drawLine(
                        color = Color(0xFF1A1A1A),
                        start = Offset(0f, y),
                        end = Offset(l.getLineRight(i) + 12.dp.toPx(), y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
        }
    )
}

@Composable
fun LoadingScreen(onFinished: () -> Unit) {
    var updateTimeString by remember { mutableStateOf("Updated 14:32") }
    var airStatusText by remember { mutableStateOf("Air data from Open-Meteo") }
    var isSyncing by remember { mutableStateOf(false) }

    // Goes to Home after 6 seconds
    LaunchedEffect(Unit) {
        delay(2500)
        onFinished()
    }

    // Updates the time every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            updateTimeString = "Updated " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color(0xFFFFFFFF),
                    0.3f to Color(0xFFFFFFFF),
                    0.7f to Color(0xFFE1FDA1),
                    1.0f to Color(0xFFE1FDA1)
                )
            )
            .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.weight(0.5f))

        Image(
            painter = painterResource(R.drawable.logo_ldg),
            contentDescription = "Living Data Garden",
            modifier = Modifier.width(200.dp)
        )

        Spacer(Modifier.weight(0.7f))

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text(
                text = updateTimeString,
                fontFamily = GardenFont,
                fontSize = 12.sp,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            RuledText(
                text = "Step into the garden where \n the air, water and light bloom \n from data around you",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.weight(0.6f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Electricity data from Electricity Maps",
                fontFamily = GardenFont,
                fontSize = 7.5.sp,
                color = Color(0xFF555555),
                maxLines = 1
            )
            Text(
                text = "Water data from CONAGUA",
                fontFamily = GardenFont,
                fontSize = 7.5.sp,
                color = Color(0xFF555555),
                maxLines = 1
            )
            Text(
                text = if (isSyncing) "Syncing..." else airStatusText,
                fontFamily = GardenFont,
                fontSize = 7.5.sp,
                color = Color(0xFF1A1A1A),
                maxLines = 1,
                modifier = Modifier.clickable {
                    isSyncing = true
                    airStatusText = "Air data updated!"
                    isSyncing = false
                }
            )
        }
    }
}