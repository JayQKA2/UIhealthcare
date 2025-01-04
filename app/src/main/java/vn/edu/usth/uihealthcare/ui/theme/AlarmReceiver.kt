package vn.edu.usth.uihealthcare.ui.theme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import vn.edu.usth.uihealthcare.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
        vibratePhone(context)
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel"
        val channelName = "Alarm Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, Sleep2Fragment::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Thời gian đi ngủ")
            .setContentText("Đến giờ đi ngủ!")
            .setSmallIcon(R.drawable.ic_alarm) // Đảm bảo biểu tượng tồn tại
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(0, notification.build())
    }

    private fun vibratePhone(context: Context) {
        val vibrator = context.getSystemService(Vibrator::class.java)
        if (vibrator != null) {
            // Chỉ sử dụng VibrationEffect cho Android O và mới hơn
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                // Ném ngoại lệ nếu không hỗ trợ
                throw UnsupportedOperationException("Vibration is not supported on this version of Android.")
            }
        }
    }
}