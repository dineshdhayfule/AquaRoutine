package com.example.alarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goal_overrides")
data class DailyGoalOverride(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val goalMl: Int
)
