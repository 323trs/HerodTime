package com.example.herodtime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetricTimeApp(viewModel: MetricTimeViewModel = viewModel()) {
    val state by viewModel.metricState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF111827)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Metric Time",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A more rational way to measure the day.",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "%d · %03d".format(state.year, state.milliday),
                color = Color(0xFF9CA3AF),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            val gradient = Brush.horizontalGradient(
                colors = listOf(Color(0xFF14B8A6), Color(0xFF3B82F6), Color(0xFF8B5CF6))
            )

            Text(
                text = "%d:%02d:%02d".format(state.metricHours, state.metricMinutes, state.metricSeconds),
                style = TextStyle(
                    brush = gradient,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "How it works", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "1 Day = 10 Hours", color = Color(0xFF9CA3AF))
                Text(text = "1 Hour = 100 Minutes", color = Color(0xFF9CA3AF))
                Text(text = "1 Minute = 100 Seconds", color = Color(0xFF9CA3AF))
            }
        }
    }
}
