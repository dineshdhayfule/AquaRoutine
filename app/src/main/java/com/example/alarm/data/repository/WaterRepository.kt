package com.example.alarm.data.repository

import com.example.alarm.data.UserPreferences
import com.example.alarm.data.dao.DailyGoalOverrideDao
import com.example.alarm.data.dao.WaterLogDao
import com.example.alarm.data.entity.DailyGoalOverride
import com.example.alarm.data.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class WaterRepository(
    private val waterLogDao: WaterLogDao,
    private val dailyGoalOverrideDao: DailyGoalOverrideDao,
    private val userPreferences: UserPreferences
) {
    fun getDailyGoalFlow(date: LocalDate): Flow<Int> {
        return combine(
            userPreferences.dailyGoalMl,
            dailyGoalOverrideDao.getOverrideForDate(date.toString())
        ) { globalGoal, override ->
            override?.goalMl ?: globalGoal
        }
    }

    suspend fun getDailyGoal(date: LocalDate): Int {
        val globalGoal = userPreferences.dailyGoalMl.first()
        val override = dailyGoalOverrideDao.getOverrideForDate(date.toString()).first()
        return override?.goalMl ?: globalGoal
    }

    suspend fun addWaterLog(amount: Int) {
        if (amount <= 0) return
        
        val today = LocalDate.now()
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        // 1. Get current intake
        val logs = waterLogDao.getLogsInRangeSuspend(start, end)
        val currentTotal = logs.sumOf { it.amountMl }
        
        // 2. Get current goal
        val globalGoal = userPreferences.dailyGoalMl.first()
        val override = dailyGoalOverrideDao.getOverrideForDate(today.toString()).first()
        val currentGoal = override?.goalMl ?: globalGoal
        
        var note: String? = null
        
        // 3. Check if goal reached/exceeded
        if (currentTotal + amount > currentGoal) {
            val newGoal = currentGoal + 1000
            dailyGoalOverrideDao.insertOverride(DailyGoalOverride(today.toString(), newGoal))
            note = "Goal reached! +1L extra added to your target."
        }
        
        // 4. Insert log
        waterLogDao.insertLog(
            WaterLogEntity(
                amountMl = amount,
                timestamp = System.currentTimeMillis(),
                note = note
            )
        )
    }

    suspend fun deleteWaterLog(log: WaterLogEntity) {
        waterLogDao.updateLog(
            log.copy(
                isDeleted = true,
                amountMl = 0,
                note = "Entry Removed",
                originalAmount = log.amountMl
            )
        )
        
        val date = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(log.timestamp), ZoneId.systemDefault())
        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        val logs = waterLogDao.getLogsInRangeSuspend(start, end)
        val newTotal = logs.sumOf { it.amountMl }
        
        val originalGoal = userPreferences.dailyGoalMl.first()
        
        val currentOverride = dailyGoalOverrideDao.getOverrideForDate(date.toString()).first()
        
        if (currentOverride != null) {
            var currentGoalOverride = currentOverride.goalMl
            var changed = false
            
            while (newTotal <= (currentGoalOverride - 1000) && currentGoalOverride > originalGoal) {
                currentGoalOverride -= 1000
                changed = true
                if (currentGoalOverride <= originalGoal) {
                    currentGoalOverride = originalGoal
                    break
                }
            }
            
            if (changed) {
                if (currentGoalOverride == originalGoal) {
                    dailyGoalOverrideDao.deleteOverrideForDate(date.toString())
                } else {
                    dailyGoalOverrideDao.insertOverride(DailyGoalOverride(date.toString(), currentGoalOverride))
                }
            }
        }
    }
}
