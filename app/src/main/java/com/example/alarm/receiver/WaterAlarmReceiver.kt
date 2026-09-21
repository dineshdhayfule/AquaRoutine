package com.example.alarm.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.alarm.model.AlarmItem
import com.example.alarm.scheduler.AlarmScheduler
import com.example.alarm.ui.AlarmActivity

class WaterAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
        rescheduleNext(context, intent)
    }

    private fun rescheduleNext(context: Context, intent: Intent) {
        val id = intent.getIntExtra("ALARM_ID", -1)
        val hour = intent.getIntExtra("HOUR", -1)
        val minute = intent.getIntExtra("MINUTE", -1)
        val daysArray = intent.getIntArrayExtra("DAYS_OF_WEEK")

        if (id != -1 && hour != -1 && minute != -1 && daysArray != null) {
            val alarmItem = AlarmItem(
                id = id,
                timeString = "",
                hour = hour,
                minute = minute,
                isActive = true,
                daysOfWeek = daysArray.toSet()
            )
            AlarmScheduler(context).scheduleAlarm(alarmItem)
        }
    }

    private fun showNotification(context: Context) {
        val channelId = "water_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Water Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Hydration alerts"
                setSound(null, null) // Sound is handled by AlarmActivity
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to Drink Water!")
            .setContentText("It's time for your hydration break.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }
}
