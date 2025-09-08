package com.example.herodtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.herodtime.ui.theme.HerodTimeTheme

class MainActivity : ComponentActivity() {
    private val vm: MetricTimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HerodTimeTheme {
                MetricTimeApp(viewModel = vm)
            }
        }
    }
}
