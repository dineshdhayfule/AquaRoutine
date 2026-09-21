package com.example.alarm.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.alarm.data.AppDatabase
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.repository.WaterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class UndoWaterAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.waterLogDao()
            val overrideDao = db.dailyGoalOverrideDao()
            val userPrefs = UserPreferences(context)
            val repository = WaterRepository(dao, overrideDao, userPrefs)

            // Calculate today's range
            val today = LocalDate.now()
            val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            val latestLog = dao.getLatestLogForDay(start, end)
            if (latestLog != null) {
                repository.deleteWaterLog(latestLog)
            }
        }
        
        // Update all instances of the widget
        WaterWidget().updateAll(context)
    }
}
