package com.example.herodtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class MetricState(
    val year: Int = 1970,
    val milliday: Int = 0,
    val metricHours: Long = 0,
    val metricMinutes: Long = 0,
    val metricSeconds: Long = 0,
) {
    val timeString: String
        get() {
            // H:MM:SS where H no leading zero, minutes and seconds two digits
            val h = metricHours.toString()
            val mm = metricMinutes.toString().padStart(2, '0')
            val ss = metricSeconds.toString().padStart(2, '0')
            return "$h:$mm:$ss"
        }
}

class MetricTimeViewModel : ViewModel() {
    private val _metricState = MutableStateFlow(MetricState())
    val metricState: StateFlow<MetricState> = _metricState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _metricState.value = calculateMetric()
                delay(100L) // update 10 times a second for smoothness
            }
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    private fun getDayOfYear(zdt: ZonedDateTime): Int {
        // Match JS behavior where Jan 1 returns 1
        return zdt.dayOfYear
    }

    private fun calculateMetric(): MetricState {
        val nowInstant = Instant.now()
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.ofInstant(nowInstant, zone)

        val startOfDay = now.toLocalDate().atStartOfDay(zone).toInstant()
        val msPassed = nowInstant.toEpochMilli() - startOfDay.toEpochMilli()

        // A standard day has 86,400,000 ms. A metric day has 1,000,000 metric seconds.
        // The ratio used in the original JS: totalMetricSeconds = Math.floor(msPassed / 864)
        val totalMetricSeconds = (msPassed / 864)

        val metricSeconds = totalMetricSeconds % 100
        val totalMetricMinutes = totalMetricSeconds / 100
        val metricMinutes = totalMetricMinutes % 100
        val metricHours = totalMetricMinutes / 100

        val year = now.year
        val dayOfYear = getDayOfYear(now)
        val daysInYear = if (isLeapYear(year)) 366 else 365
        val milliday = ((dayOfYear.toDouble() / daysInYear.toDouble()) * 1000.0).toInt()

        return MetricState(
            year = year,
            milliday = milliday,
            metricHours = metricHours,
            metricMinutes = metricMinutes,
            metricSeconds = metricSeconds
        )
    }
}
