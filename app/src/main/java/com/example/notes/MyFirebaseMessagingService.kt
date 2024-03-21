package com.example.notes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService :FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body

            // Create and show notification
            sendNotification(title,body)
        }
    }
    private fun sendNotification(title: String?,body: String?){
        //Customize notification creation
        val notificationBuilder = NotificationCompat.Builder(this,"channel_id")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.circle_notifications_icon)
            .setAutoCancel(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //create a unique notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        // Show the push notification
        notificationManager.notify(0,notificationBuilder.build())
    }
}
