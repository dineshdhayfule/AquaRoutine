package com.example.alarm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.data.AppDatabase
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.entity.AlarmEntity
import com.example.alarm.model.AlarmItem
import com.example.alarm.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel for managing water intake alarms.
 */
class WaterIntakeViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)
    private val userPreferences = UserPreferences(application)
    private val alarmDao = AppDatabase.getDatabase(application).alarmDao()

    val alarms: StateFlow<List<AlarmItem>> = alarmDao.getAllAlarms()
        .map { entities ->
            entities.map { it.toAlarmItem() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val snoozeDurationMin: StateFlow<Int> = userPreferences.snoozeDurationMin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.DEFAULT_SNOOZE_MIN)

    init {
        viewModelScope.launch {
            if (alarmDao.getAllAlarmsList().isEmpty()) {
                initializeDefaultAlarms()
            }
        }
    }

    private suspend fun initializeDefaultAlarms() {
        val defaultHours = listOf(9, 11, 13, 15, 17, 19, 21, 23)
        defaultHours.forEach { hour ->
            val entity = AlarmEntity(
                hour = hour,
                minute = 0,
                isActive = true,
                isCustom = false,
                daysOfWeek = "1,2,3,4,5,6,7"
            )
            val id = alarmDao.insertAlarm(entity)
            alarmScheduler.scheduleAlarm(entity.toAlarmItem(id.toInt()))
        }
    }

    /**
     * Toggles the active state of a specific alarm.
     */
    fun toggleAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isActive = !alarm.isActive)
            alarmDao.updateAlarm(updatedAlarm.toEntity())
            if (updatedAlarm.isActive) {
                alarmScheduler.scheduleAlarm(updatedAlarm)
            } else {
                alarmScheduler.cancelAlarm(updatedAlarm)
            }
        }
    }

    /**
     * Toggles a specific day for an alarm.
     */
    fun toggleDay(alarmId: Int, day: Int) {
        viewModelScope.launch {
            val currentAlarms = alarms.value
            val alarm = currentAlarms.find { it.id == alarmId } ?: return@launch
            
            val updatedDays = if (alarm.daysOfWeek.contains(day)) {
                alarm.daysOfWeek - day
            } else {
                alarm.daysOfWeek + day
            }
            val updatedAlarm = alarm.copy(daysOfWeek = updatedDays)
            
            alarmDao.updateAlarm(updatedAlarm.toEntity())

            // Re-schedule
            if (updatedAlarm.isActive) {
                alarmScheduler.cancelAlarm(alarm)
                alarmScheduler.scheduleAlarm(updatedAlarm)
            }
        }
    }

    /**
     * Adds a custom alarm.
     */
    fun addCustomAlarm(hour: Int, minute: Int) {
        viewModelScope.launch {
            val entity = AlarmEntity(
                hour = hour,
                minute = minute,
                isActive = true,
                isCustom = true,
                daysOfWeek = "1,2,3,4,5,6,7"
            )
            val id = alarmDao.insertAlarm(entity)
            val newAlarm = entity.toAlarmItem(id.toInt())
            if (newAlarm.isActive) {
                alarmScheduler.scheduleAlarm(newAlarm)
            }
        }
    }

    /**
     * Toggles all alarms at once.
     */
    fun toggleMaster(enabled: Boolean) {
        viewModelScope.launch {
            val currentAlarms = alarms.value
            currentAlarms.forEach { alarm ->
                val updatedAlarm = alarm.copy(isActive = enabled)
                alarmDao.updateAlarm(updatedAlarm.toEntity())
                if (enabled) {
                    alarmScheduler.scheduleAlarm(updatedAlarm)
                } else {
                    alarmScheduler.cancelAlarm(updatedAlarm)
                }
            }
        }
    }

    /**
     * Deletes a custom alarm.
     */
    fun deleteAlarm(alarm: AlarmItem) {
        if (alarm.isCustom) {
            viewModelScope.launch {
                alarmScheduler.cancelAlarm(alarm)
                alarmDao.deleteAlarm(alarm.toEntity())
            }
        }
    }

    /**
     * Updates an existing alarm.
     */
    fun updateAlarm(alarmId: Int, newHour: Int, newMinute: Int) {
        viewModelScope.launch {
            val currentAlarms = alarms.value
            val alarm = currentAlarms.find { it.id == alarmId } ?: return@launch
            
            // Cancel old alarm schedule
            alarmScheduler.cancelAlarm(alarm)
            
            val updatedAlarm = alarm.copy(
                hour = newHour,
                minute = newMinute,
                timeString = formatTimeString(newHour, newMinute)
            )
            
            alarmDao.updateAlarm(updatedAlarm.toEntity())
            
            // Schedule updated alarm if active
            if (updatedAlarm.isActive) {
                alarmScheduler.scheduleAlarm(updatedAlarm)
            }
        }
    }

    private fun AlarmEntity.toAlarmItem(overrideId: Int? = null): AlarmItem {
        val actualId = overrideId ?: this.id
        return AlarmItem(
            id = actualId,
            timeString = formatTimeString(hour, minute),
            hour = hour,
            minute = minute,
            isActive = isActive,
            isCustom = isCustom,
            daysOfWeek = daysOfWeek.split(",").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()
        )
    }

    private fun AlarmItem.toEntity(): AlarmEntity {
        return AlarmEntity(
            id = id,
            hour = hour,
            minute = minute,
            isActive = isActive,
            isCustom = isCustom,
            daysOfWeek = daysOfWeek.joinToString(",")
        )
    }

    private fun formatTimeString(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
    }
}
