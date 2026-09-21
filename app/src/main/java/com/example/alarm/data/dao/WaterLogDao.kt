package com.example.alarm.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.alarm.data.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Insert
    suspend fun insertLog(log: WaterLogEntity)

    @Update
    suspend fun updateLog(log: WaterLogEntity)

    @Delete
    suspend fun deleteLog(log: WaterLogEntity)

    @Query("SELECT * FROM water_logs WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    fun getLogsForDay(start: Long, end: Long): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLogForDay(start: Long, end: Long): WaterLogEntity?

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE timestamp >= :start AND timestamp <= :end")
    fun getTotalConsumedForDay(start: Long, end: Long): Flow<Int?>

    @Query("SELECT * FROM water_logs WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp ASC")
    fun getLogsInRange(start: Long, end: Long): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp ASC")
    suspend fun getLogsInRangeSuspend(start: Long, end: Long): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs ORDER BY timestamp ASC")
    suspend fun getAllLogs(): List<WaterLogEntity>

    @Query("DELETE FROM water_logs")
    suspend fun deleteAllLogs()
}
