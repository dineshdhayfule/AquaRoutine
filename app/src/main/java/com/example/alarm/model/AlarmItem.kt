package com.example.alarm.model

/**
 * Data class representing an alarm for water intake.
 *
 * @property id Unique identifier for the alarm.
 * @property timeString Formatted time string (e.g., "09:00 AM").
 * @property hour Hour in 24-hour format (0-23).
 * @property minute Minute (0-59).
 * @property isActive Whether the alarm is currently enabled.
 * @property isCustom Whether this is a user-added custom alarm.
 */
data class AlarmItem(
    val id: Int,
    val timeString: String,
    val hour: Int,
    val minute: Int,
    val isActive: Boolean = true,
    val isCustom: Boolean = false,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7)
)
