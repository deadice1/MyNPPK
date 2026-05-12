package com.example.schedule.feature.schedule.ui

import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import com.example.schedule.shared.ui.util.NotificationHelper

class ScheduleFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        android.util.Log.d("FIREBASE_TEST", "📩 ПРИШЕЛ ПУШ ОТ СЕРВЕРА: ${remoteMessage.data}")

        val title = remoteMessage.data["title"] ?: "Обновление расписания"
        val body = remoteMessage.data["body"] ?: "Проверьте изменения на завтра"
        val targetDate = remoteMessage.data["target_date"]
        
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("TARGET_DATE", targetDate)
        }
        
        NotificationHelper.showNotification(
            context = this,
            title = title,
            message = body,
            intent = intent
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}