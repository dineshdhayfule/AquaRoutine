package com.example.alarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isActive: Boolean,
    val isCustom: Boolean,
    val daysOfWeek: String // Comma separated days (1,2,3,4,5,6,7)
)
