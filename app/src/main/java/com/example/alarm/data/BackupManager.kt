package com.example.alarm.data

import android.content.Context
import android.net.Uri
import com.example.alarm.data.entity.AlarmEntity
import com.example.alarm.data.entity.WaterLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val waterLogDao = db.waterLogDao()
    private val alarmDao = db.alarmDao()
    private val userPrefs = UserPreferences(context)

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject()
            json.put("version", 1)

            // Water Logs
            val logs = waterLogDao.getAllLogs()
            val logsArray = JSONArray()
            logs.forEach { log ->
                val logJson = JSONObject()
                logJson.put("amountMl", log.amountMl)
                logJson.put("timestamp", log.timestamp)
                logsArray.put(logJson)
            }
            json.put("waterLogs", logsArray)

            // Alarms
            val alarms = alarmDao.getAllAlarmsList()
            val alarmsArray = JSONArray()
            alarms.forEach { alarm ->
                val alarmJson = JSONObject()
                alarmJson.put("hour", alarm.hour)
                alarmJson.put("minute", alarm.minute)
                alarmJson.put("isActive", alarm.isActive)
                alarmJson.put("isCustom", alarm.isCustom)
                alarmJson.put("daysOfWeek", alarm.daysOfWeek)
                alarmsArray.put(alarmJson)
            }
            json.put("alarms", alarmsArray)

            // Preferences
            val prefsJson = JSONObject()
            prefsJson.put("dailyGoalMl", userPrefs.dailyGoalMl.first())
            prefsJson.put("userWeightKg", userPrefs.userWeightKg.first().toDouble())
            prefsJson.put("activityLevel", userPrefs.activityLevel.first())
            prefsJson.put("snoozeDurationMin", userPrefs.snoozeDurationMin.first())
            json.put("preferences", prefsJson)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json.toString(4))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).readText()
            } ?: return@withContext Result.failure(Exception("Could not read file"))

            val json = JSONObject(content)
            
            // Import Water Logs
            val logsArray = json.optJSONArray("waterLogs")
            if (logsArray != null) {
                for (i in 0 until logsArray.length()) {
                    val logJson = logsArray.getJSONObject(i)
                    waterLogDao.insertLog(
                        WaterLogEntity(
                            amountMl = logJson.getInt("amountMl"),
                            timestamp = logJson.getLong("timestamp")
                        )
                    )
                }
            }

            // Import Alarms
            val alarmsArray = json.optJSONArray("alarms")
            if (alarmsArray != null) {
                for (i in 0 until alarmsArray.length()) {
                    val alarmJson = alarmsArray.getJSONObject(i)
                    alarmDao.insertAlarm(
                        AlarmEntity(
                            hour = alarmJson.getInt("hour"),
                            minute = alarmJson.getInt("minute"),
                            isActive = alarmJson.getBoolean("isActive"),
                            isCustom = alarmJson.getBoolean("isCustom"),
                            daysOfWeek = alarmJson.getString("daysOfWeek")
                        )
                    )
                }
            }

            // Import Preferences
            val prefsJson = json.optJSONObject("preferences")
            if (prefsJson != null) {
                userPrefs.saveDailyGoalMl(prefsJson.getInt("dailyGoalMl"))
                userPrefs.saveUserProfile(
                    prefsJson.getDouble("userWeightKg").toFloat(),
                    prefsJson.getString("activityLevel")
                )
                userPrefs.saveSnoozeDuration(prefsJson.getInt("snoozeDurationMin"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            waterLogDao.deleteAllLogs()
            alarmDao.deleteAllAlarms()
            // Reset preferences if needed, or keep them? 
            // Usually "Delete All Data" means logs and custom stuff.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
