package vn.edu.usth.uihealthcare.noti

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.MainActivity

class NotificationsHelper : BroadcastReceiver() {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "general_notification_channel"
        private const val NOTIFICATION_ID = 1

        fun createNotificationChannel(context: Context) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Step Counter Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }

            notificationManager.createNotificationChannel(channel)
        }

        fun buildNotification(context: Context, stepCount: Int): Notification {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Step Counter Running")
                .setContentText("Steps: $stepCount")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }

        fun updateNotification(context: Context, stepCount: Int) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val notification = buildNotification(context, stepCount)
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.hasExtra("step_count") == true) {
            val stepCount = intent.getIntExtra("step_count", 0)
            updateNotification(context, stepCount)
        }
    }
}
