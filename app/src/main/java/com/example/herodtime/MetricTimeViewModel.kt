package com.example.herodtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    // Events for the Activity to observe (notifications / sounds)
    sealed class NotificationEvent {
        object TimerFinished : NotificationEvent()
        object AlarmTriggered : NotificationEvent()
    data class Test(val title: String, val message: String) : NotificationEvent()
    }

    private val _events = MutableSharedFlow<NotificationEvent>()
    val events: SharedFlow<NotificationEvent> = _events.asSharedFlow()

    // Helper to emit a test notification event from UI for debugging
    fun emitTestNotification(title: String, message: String) {
        viewModelScope.launch {
            _events.emit(NotificationEvent.Test(title, message))
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                val base = calculateMetric()
                val prev = _metricState.value
                // preserve timer and alarm fields across recalculations
                _metricState.value = base.copy(
                    timerRunning = prev.timerRunning,
                    timerSecondsLeft = prev.timerSecondsLeft,
                    alarmSet = prev.alarmSet,
                    alarmHour = prev.alarmHour,
                    alarmMinute = prev.alarmMinute,
                    alarmTriggered = prev.alarmTriggered
                )
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
        _metricState.value = _metricState.value.copy(timerRunning = true, timerSecondsLeft = seconds)
        timerJob = viewModelScope.launch {
            while (_metricState.value.timerSecondsLeft > 0 && _metricState.value.timerRunning) {
                delay(1000L)
                _metricState.value = _metricState.value.copy(timerSecondsLeft = _metricState.value.timerSecondsLeft - 1)
            }
                // timer finished
                _metricState.value = _metricState.value.copy(timerRunning = false)
                if (_metricState.value.timerSecondsLeft <= 0L) {
                    _events.tryEmit(NotificationEvent.TimerFinished)
                }
        }
    }

    fun stopTimer() {
    timerJob?.cancel()
    _metricState.value = _metricState.value.copy(timerRunning = false)
    }

    // Alarm logic
    fun setAlarm(hour: Int, minute: Int) {
        _metricState.value = _metricState.value.copy(
            alarmSet = true,
            alarmHour = hour,
            alarmMinute = minute,
            alarmTriggered = false
        )
    }

    fun clearAlarm() {
    _metricState.value = _metricState.value.copy(alarmSet = false, alarmTriggered = false)
    }

    private fun checkAlarm() {
        val s = _metricState.value
        if (s.alarmSet && !s.alarmTriggered) {
            val h = s.metricHours.toInt()
            val m = s.metricMinutes.toInt()
            if (h == s.alarmHour && m == s.alarmMinute) {
                _metricState.value = s.copy(alarmTriggered = true)
                viewModelScope.launch { _events.emit(NotificationEvent.AlarmTriggered) }
            }
        }
    }
}
