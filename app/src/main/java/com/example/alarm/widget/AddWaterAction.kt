package com.example.alarm.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.alarm.data.AppDatabase
import com.example.alarm.data.entity.WaterLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.alarm.data.UserPreferences
import com.example.alarm.data.repository.WaterRepository

class AddWaterAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val amount = parameters[AmountKey] ?: 250
        if (amount <= 0) return
        
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val waterLogDao = db.waterLogDao()
            val dailyGoalOverrideDao = db.dailyGoalOverrideDao()
            val userPrefs = UserPreferences(context)
            
            val repository = WaterRepository(waterLogDao, dailyGoalOverrideDao, userPrefs)
            repository.addWaterLog(amount)
        }
        
        // Update all instances of the widget
        WaterWidget().updateAll(context)
    }

    companion object {
        val AmountKey = ActionParameters.Key<Int>("amount")
    }
}
