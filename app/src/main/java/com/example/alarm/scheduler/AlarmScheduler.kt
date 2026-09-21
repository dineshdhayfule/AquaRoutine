package com.example.alarm.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.alarm.model.AlarmItem
import com.example.alarm.receiver.WaterAlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val SNOOZE_REQUEST_CODE = 9999
    }

    fun scheduleAlarm(alarm: AlarmItem) {
        if (!alarm.isActive || alarm.daysOfWeek.isEmpty()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        val intent = Intent(context, WaterAlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("HOUR", alarm.hour)
            putExtra("MINUTE", alarm.minute)
            putExtra("DAYS_OF_WEEK", alarm.daysOfWeek.toIntArray())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = calculateNextOccurrence(alarm.hour, alarm.minute, alarm.daysOfWeek)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun scheduleSnooze(durationMinutes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        val intent = Intent(context, WaterAlarmReceiver::class.java).apply {
            putExtra("IS_SNOOZE", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    private fun calculateNextOccurrence(hour: Int, minute: Int, daysOfWeek: Set<Int>): Calendar {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()
        
        // Check if today is one of the days
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        
        if (daysOfWeek.contains(today) && (calendar.timeInMillis > now)) {
            // It's today and still in the future
            return calendar
        }

        // Otherwise, find the next selected day
        repeat(7) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (daysOfWeek.contains(dayOfWeek)) {
                return calendar
            }
        }

        return calendar // Fallback (should not happen if daysOfWeek is not empty)
    }

    fun cancelAlarm(alarm: AlarmItem) {
        val intent = Intent(context, WaterAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
