package com.example.alarm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.alarm.data.entity.DailyGoalOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalOverrideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: DailyGoalOverride)

    @Query("SELECT * FROM daily_goal_overrides WHERE date = :date")
    fun getOverrideForDate(date: String): Flow<DailyGoalOverride?>

    @Query("SELECT * FROM daily_goal_overrides WHERE date >= :startDate AND date <= :endDate")
    fun getOverridesInRange(startDate: String, endDate: String): Flow<List<DailyGoalOverride>>

    @Query("DELETE FROM daily_goal_overrides WHERE date = :date")
    suspend fun deleteOverrideForDate(date: String)
}
