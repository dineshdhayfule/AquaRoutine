package com.example.alarm.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.data.BackupManager
import com.example.alarm.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application, private val userPreferences: UserPreferences) : AndroidViewModel(application) {

    private val backupManager = BackupManager(application)

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val dailyGoalMl: StateFlow<Int> = userPreferences.dailyGoalMl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.DEFAULT_GOAL_ML)

    val userWeightKg: StateFlow<Float> = userPreferences.userWeightKg
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.DEFAULT_WEIGHT_KG)

    val activityLevel: StateFlow<String> = userPreferences.activityLevel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.DEFAULT_ACTIVITY_LEVEL)

    val snoozeDurationMin: StateFlow<Int> = userPreferences.snoozeDurationMin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.DEFAULT_SNOOZE_MIN)

    val alarmSoundName: StateFlow<String> = userPreferences.alarmSoundName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Default")

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            userPreferences.saveDailyGoalMl(goal)
        }
    }

    fun updateUserProfile(weight: Float, activity: String) {
        viewModelScope.launch {
            userPreferences.saveUserProfile(weight, activity)
            // Recalculate and update goal automatically based on profile
            val baseGoal = (weight * 35).toInt()
            val adjustment = when (activity) {
                "Active" -> 500
                "Very Active" -> 1000
                else -> 0
            }
            userPreferences.saveDailyGoalMl(baseGoal + adjustment)
        }
    }

    fun updateSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            userPreferences.saveSnoozeDuration(minutes)
        }
    }

    fun updateAlarmSound(uri: String?, name: String?) {
        viewModelScope.launch {
            userPreferences.saveAlarmSound(uri, name)
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.exportData(uri)
            _statusMessage.value = if (result.isSuccess) "Data exported successfully" else "Export failed: ${result.exceptionOrNull()?.message}"
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.importData(uri)
            _statusMessage.value = if (result.isSuccess) "Data imported successfully" else "Import failed: ${result.exceptionOrNull()?.message}"
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            val result = backupManager.deleteAllData()
            _statusMessage.value = if (result.isSuccess) "All data deleted" else "Deletion failed: ${result.exceptionOrNull()?.message}"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
