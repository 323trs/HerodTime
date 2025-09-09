package com.example.herodtime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.setValue
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

            Spacer(modifier = Modifier.height(16.dp))

            // Controls: Timer and Alarm
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Timer", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                TimerControls(state = state, onStart = { secs -> viewModel.startTimer(secs) }, onStop = { viewModel.stopTimer() })

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Alarm", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                AlarmControls(state = state, onSet = { h, m -> viewModel.setAlarm(h, m) }, onClear = { viewModel.clearAlarm() })

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "How it works", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "1 Day = 10 Hours", color = Color(0xFF9CA3AF))
                Text(text = "1 Hour = 100 Minutes", color = Color(0xFF9CA3AF))
                Text(text = "1 Minute = 100 Seconds", color = Color(0xFF9CA3AF))
            }
        }
    }
}

@Composable
private fun TimerControls(state: MetricState, onStart: (Long) -> Unit, onStop: () -> Unit) {
    var input by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter { ch -> ch.isDigit() } },
            label = { Text("seconds") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { val s = input.toLongOrNull() ?: 0L; onStart(s) }) { Text("Start") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onStop) { Text("Stop") }
    }
}

@Composable
private fun AlarmControls(state: MetricState, onSet: (Int, Int) -> Unit, onClear: () -> Unit) {
    var hour by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var minute by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = hour, onValueChange = { hour = it.filter { ch -> ch.isDigit() } }, label = { Text("hr") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.width(72.dp))
        Spacer(modifier = Modifier.width(6.dp))
        OutlinedTextField(value = minute, onValueChange = { minute = it.filter { ch -> ch.isDigit() } }, label = { Text("min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.width(72.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onSet(hour.toIntOrNull() ?: 0, minute.toIntOrNull() ?: 0) }) { Text("Set") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onClear) { Text("Clear") }
    }
}
