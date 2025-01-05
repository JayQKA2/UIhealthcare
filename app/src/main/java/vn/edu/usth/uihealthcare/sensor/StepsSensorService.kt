package vn.edu.usth.uihealthcare.sensor

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.ZonedDateTime
import kotlin.math.sqrt

class StepsSensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var steps = 0
    private var previousMagnitude = 0.0
    private val threshold = 12.0
    private var stepCount = 0
    private var isHaveStepCounter = true
    private var lastStepTime: Long = 0
    private val stepInterval = 50
    private lateinit var healthConnectManager: HealthConnectManager

    companion object {
        private const val TAG = "Steps"
        private const val CHANNEL_ID = "steps_sensor_service_channel"
    }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        val context = applicationContext
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        healthConnectManager = HealthConnectManager(context)

        createNotificationChannel()
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).also {
                isHaveStepCounter = false
            }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d(TAG, "Service started and sensor registered.")

        startForeground(1, createNotification(0))
    }

    private fun createNotification(stepCount: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Steps Sensor Running: $stepCount steps")
            .setContentText("Tracking your steps in the background")
            .setSmallIcon(R.drawable.ic_steps)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(stepCount: Int) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, createNotification(stepCount))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Steps Sensor Service",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for Steps Sensor Foreground Service"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isHaveStepCounter) {
            handleStepCounter(event)
        } else {
            handleAccelerometer(event)
        }
    }

    private fun handleStepCounter(event: SensorEvent) {
        val eventSteps = event.values[0].toInt()
        if (steps == 0) {
            steps = eventSteps
        } else {
            steps = eventSteps
            sendStepCountToFragment(steps)
            updateNotification(steps)

            val startTime = ZonedDateTime.now()
            val endTime = startTime.plusMinutes(1)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    healthConnectManager.writeStepsInput(startTime, endTime, steps.toLong())        // If using step counter

//                    Log.d(TAG, "Successfully updated ")
                } catch (e: Exception) {
//                    Log.e(TAG, "Error writing steps data: ${e.message}")
                }
            }
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()

            val magnitude = sqrt(x * x + y * y + z * z)
            val delta = magnitude - previousMagnitude
            previousMagnitude = magnitude

            if (isStep(delta)) {
                stepCount++
                sendStepCountToFragment(stepCount)
                updateNotification(stepCount)
            }

            val startTime = ZonedDateTime.now()
            val endTime = startTime.plusMinutes(1)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    healthConnectManager.writeStepsInput(startTime, endTime, stepCount.toLong())        // If using accelerometer

//                    Log.d(TAG, "Successfully updated ")
                } catch (e: Exception) {
//                    Log.e(TAG, "Error writing steps data: ${e.message}")
                }
            }
        }
    }

    private fun isStep(delta: Double): Boolean {
        val currentTime = System.currentTimeMillis()
        if (delta > threshold && currentTime - lastStepTime > stepInterval) {
            lastStepTime = currentTime
            return true
        }
        return false
    }

    private fun sendStepCountToFragment(stepCount: Int) {
        val intent = Intent("vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE")
        intent.putExtra("step_count", stepCount)
        sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Service stopped and sensor unregistered.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}