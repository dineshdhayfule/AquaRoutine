package com.example.alarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountMl: Int,
    val timestamp: Long,
    val note: String? = null,
    val isDeleted: Boolean = false,
    val originalAmount: Int = 0,
    val source: String = "LIVE",
    val createdAt: Long = System.currentTimeMillis()
)
