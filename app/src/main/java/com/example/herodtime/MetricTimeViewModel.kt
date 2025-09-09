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
    var timerRunning: Boolean = false,
    var timerSecondsLeft: Long = 0,
    var alarmSet: Boolean = false,
    var alarmHour: Int = 0,
    var alarmMinute: Int = 0,
    var alarmTriggered: Boolean = false
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

    // Million day mode: true = 10h day, false = 24h day
    var millionDayMode: Boolean = false

    // Timer
    private var timerJob: kotlinx.coroutines.Job? = null

    // Alarm
    private var alarmJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            while (true) {
                _metricState.value = calculateMetric()
                checkAlarm()
                delay(100L)
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

        val year = now.year
        val dayOfYear = getDayOfYear(now)
        val daysInYear = if (isLeapYear(year)) 366 else 365
        val milliday = ((dayOfYear.toDouble() / daysInYear.toDouble()) * 1000.0).toInt()

        // Million day logic
        val hoursInDay = if (millionDayMode) 10 else 24
        val minutesInHour = 60
        val secondsInMinute = 60
        val msInDay = hoursInDay * minutesInHour * secondsInMinute * 1000

        val msToday = msPassed % msInDay
        val hour = (msToday / (minutesInHour * secondsInMinute * 1000)).toInt()
        val minute = ((msToday / (secondsInMinute * 1000)) % minutesInHour).toInt()
        val second = ((msToday / 1000) % secondsInMinute).toInt()

        return MetricState(
            year = year,
            milliday = milliday,
            metricHours = hour.toLong(),
            metricMinutes = minute.toLong(),
            metricSeconds = second.toLong()
        )
    }

    // Timer logic
    fun startTimer(seconds: Long) {
        timerJob?.cancel()
        _metricState.value.timerRunning = true
        _metricState.value.timerSecondsLeft = seconds
        timerJob = viewModelScope.launch {
            while (_metricState.value.timerSecondsLeft > 0 && _metricState.value.timerRunning) {
                delay(1000L)
                _metricState.value = _metricState.value.copy(timerSecondsLeft = _metricState.value.timerSecondsLeft - 1)
            }
            _metricState.value.timerRunning = false
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _metricState.value.timerRunning = false
    }

    // Alarm logic
    fun setAlarm(hour: Int, minute: Int) {
        _metricState.value.alarmSet = true
        _metricState.value.alarmHour = hour
        _metricState.value.alarmMinute = minute
        _metricState.value.alarmTriggered = false
    }

    fun clearAlarm() {
        _metricState.value.alarmSet = false
        _metricState.value.alarmTriggered = false
    }

    private fun checkAlarm() {
        if (_metricState.value.alarmSet && !_metricState.value.alarmTriggered) {
            val h = _metricState.value.metricHours.toInt()
            val m = _metricState.value.metricMinutes.toInt()
            if (h == _metricState.value.alarmHour && m == _metricState.value.alarmMinute) {
                _metricState.value.alarmTriggered = true
                // You can add notification or sound logic here
            }
        }
    }
}
