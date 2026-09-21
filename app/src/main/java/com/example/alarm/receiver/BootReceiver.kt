package com.example.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.data.AppDatabase
import com.example.alarm.data.entity.AlarmEntity
import com.example.alarm.model.AlarmItem
import com.example.alarm.scheduler.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAlarms(context)
        }
    }

    private fun rescheduleAlarms(context: Context) {
        val alarmScheduler = AlarmScheduler(context)
        val alarmDao = AppDatabase.getDatabase(context).alarmDao()

        CoroutineScope(Dispatchers.IO).launch {
            val alarms = alarmDao.getAllAlarmsList()
            alarms.forEach { entity ->
                if (entity.isActive) {
                    alarmScheduler.scheduleAlarm(entity.toAlarmItem())
                }
            }
        }
    }

    private fun AlarmEntity.toAlarmItem(): AlarmItem {
        return AlarmItem(
            id = id,
            timeString = formatTimeString(hour, minute),
            hour = hour,
            minute = minute,
            isActive = isActive,
            isCustom = isCustom,
            daysOfWeek = daysOfWeek.split(",").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()
        )
    }

    private fun formatTimeString(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
    }
}
