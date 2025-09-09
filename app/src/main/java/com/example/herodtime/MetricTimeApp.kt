package com.example.herodtime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

// Format seconds as H:MM:SS (if hour >0) or MM:SS
fun formatSeconds(sec: Long): String {
    if (sec <= 0) return "00:00"
    val s = sec % 60
    val m = (sec / 60) % 60
    val h = sec / 3600
    return if (h > 0) {
        "%d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}

@Composable
fun TimerControls(state: MetricState, onStart: (Long) -> Unit, onStop: () -> Unit) {
    var input by remember { mutableStateOf("") }
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
fun AlarmControls(state: MetricState, onSet: (Int, Int) -> Unit, onClear: () -> Unit) {
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }
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

@Composable
fun MetricTimeApp(
    viewModel: MetricTimeViewModel = viewModel(),
    onOpenSettings: () -> Unit = {},
    onTest: () -> Unit = {}
) {
    val state by viewModel.metricState.collectAsState()
    val scaffoldState = rememberScaffoldState()

    // show snackbars for Test events emitted from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { ev ->
            when (ev) {
                is MetricTimeViewModel.NotificationEvent.Test -> {
                    scaffoldState.snackbarHostState.showSnackbar(ev.message)
                }
                else -> { /* handled by Activity: system notification + sound */ }
            }
        }
    }

    Scaffold(scaffoldState = scaffoldState) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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

                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.timerRunning) {
                        Text(text = formatSeconds(state.timerSecondsLeft), color = Color(0xFF10B981), fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Debug/Test controls
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = onTest) { Text("Test Notification") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onOpenSettings) { Text("Open Notification Settings") }
                    }

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
}
