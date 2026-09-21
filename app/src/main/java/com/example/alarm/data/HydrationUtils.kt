package com.example.alarm.data

import com.example.alarm.data.entity.WaterLogEntity
import java.time.LocalDate
import java.time.ZoneId

object HydrationUtils {
    fun calculateStreak(logs: List<WaterLogEntity>, goalMl: Int): Int {
        if (goalMl <= 0) return 0
        
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        
        // Group logs by date
        val logsByDate = logs.groupBy {
            LocalDate.ofInstant(java.time.Instant.ofEpochMilli(it.timestamp), zoneId)
        }

        var streak = 0
        var checkDate = today

        // Check today's total
        val todayTotal = logsByDate[today]?.sumOf { it.amountMl } ?: 0
        if (todayTotal >= goalMl) {
            streak++
            checkDate = today.minusDays(1)
        } else {
            // Streak still active if yesterday was reached
            checkDate = today.minusDays(1)
        }

        while (true) {
            val dailyTotal = logsByDate[checkDate]?.sumOf { it.amountMl } ?: 0
            if (dailyTotal >= goalMl) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }
}
