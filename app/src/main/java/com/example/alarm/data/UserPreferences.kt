package com.example.alarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val USER_WEIGHT_KG = floatPreferencesKey("user_weight_kg")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val SNOOZE_DURATION_MIN = intPreferencesKey("snooze_duration_min")
        val WIDGET_INCREMENT_ML = intPreferencesKey("widget_increment_ml")
        val ALARM_SOUND_URI = stringPreferencesKey("alarm_sound_uri")
        val ALARM_SOUND_NAME = stringPreferencesKey("alarm_sound_name")
        
        const val DEFAULT_GOAL_ML = 2500
        const val DEFAULT_WEIGHT_KG = 70.0f
        const val DEFAULT_ACTIVITY_LEVEL = "Sedentary"
        const val DEFAULT_SNOOZE_MIN = 10
        const val DEFAULT_INCREMENT_ML = 250
    }

    val dailyGoalMl: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DAILY_GOAL_ML] ?: DEFAULT_GOAL_ML
        }

    val userWeightKg: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[USER_WEIGHT_KG] ?: DEFAULT_WEIGHT_KG
        }

    val activityLevel: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[ACTIVITY_LEVEL] ?: DEFAULT_ACTIVITY_LEVEL
        }

    val snoozeDurationMin: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[SNOOZE_DURATION_MIN] ?: DEFAULT_SNOOZE_MIN
        }

    val widgetIncrementMl: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[WIDGET_INCREMENT_ML] ?: DEFAULT_INCREMENT_ML
        }

    val alarmSoundUri: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ALARM_SOUND_URI]
        }

    val alarmSoundName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[ALARM_SOUND_NAME] ?: "Default"
        }

    suspend fun saveDailyGoalMl(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_GOAL_ML] = goal
        }
    }

    suspend fun saveUserProfile(weight: Float, activity: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_WEIGHT_KG] = weight
            preferences[ACTIVITY_LEVEL] = activity
        }
    }

    suspend fun saveSnoozeDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[SNOOZE_DURATION_MIN] = minutes
        }
    }

    suspend fun saveWidgetIncrementMl(amount: Int) {
        context.dataStore.edit { preferences ->
            preferences[WIDGET_INCREMENT_ML] = amount
        }
    }

    suspend fun saveAlarmSound(uri: String?, name: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[ALARM_SOUND_URI] = uri
            } else {
                preferences.remove(ALARM_SOUND_URI)
            }
            if (name != null) {
                preferences[ALARM_SOUND_NAME] = name
            } else {
                preferences.remove(ALARM_SOUND_NAME)
            }
        }
    }
}
