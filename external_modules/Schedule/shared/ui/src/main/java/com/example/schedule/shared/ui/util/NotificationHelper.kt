package com.example.schedule.shared.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID = "nppk_general_channel"
    private const val CHANNEL_NAME = "Общие уведомления"

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt(),
        intent: Intent? = null
    ) {
        // Центральная проверка настройки уведомлений
        val prefs = context.getSharedPreferences("nppk_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("notifications_enabled", true)
        
        if (!isEnabled) {
            android.util.Log.d("NOTIFICATION_BLOCK", "🚫 Уведомление заблокировано настройками: $title")
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал для Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления приложения НППК"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent? = intent?.let {
            PendingIntent.getActivity(
                context,
                notificationId,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // Пытаемся получить иконку приложения динамически
        val appIcon = context.applicationInfo.icon

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (appIcon != 0) appIcon else android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        pendingIntent?.let { builder.setContentIntent(it) }

        notificationManager.notify(notificationId, builder.build())
    }
}
