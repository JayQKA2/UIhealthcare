package vn.edu.usth.uihealthcare.sensor

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import vn.edu.usth.uihealthcare.noti.NotificationsHelper
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.sqrt

class StepsSensorService : Service(), SensorEventListener {

    private val binder = LocalBinder()
    private lateinit var sensorManager: SensorManager
    private var stepCount = 0
    private var previousMagnitude = 0.0
    private val threshold = 5.0
    private val stepInterval = 400
    private var lastStepTime: Long = 0
    private var isHaveStepCounter = true
    private var initialStepCount: Int = -1
    private lateinit var healthConnectManager: HealthConnectManager
    private val _stepCountFlow = MutableStateFlow(0)
    val stepCountFlow = _stepCountFlow.asStateFlow()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var lastRecordedDate: LocalDate = LocalDate.now()

    companion object {
        private const val TAG = "StepsSensorService"
        private const val NOTIFICATION_ID = 1
    }

    inner class LocalBinder : Binder() {
        fun getService(): StepsSensorService = this@StepsSensorService
    }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate() called.")
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        healthConnectManager = HealthConnectManager(applicationContext)

        lastRecordedDate = LocalDate.now()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand() called.")
        startAsForegroundService()

        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (stepSensor == null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            Log.e(TAG, " Step counter found ")
        } else if (accelSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.e(TAG, " Accelerometer found ")
            isHaveStepCounter = false
        } else {
            Log.e(TAG, "No compatible sensor found.")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onSensorChanged(event: SensorEvent) {
        checkForNewDay()
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                handleStepCounter(event)
            }

            Sensor.TYPE_ACCELEROMETER -> {
//                Log.d(TAG, "Accelerometer sensor detected. Values: x=${event.values[0]}, y=${event.values[1]}, z=${event.values[2]}")
                handleAccelerometer(event)
            }

            else -> {
                Log.d(TAG, "Unknown sensor type: ${event.sensor.type}")
            }
        }
    }

    private fun handleStepCounter(event: SensorEvent) {
        val currentStepCount = event.values[0].toInt()

        if (initialStepCount == -1) {
            initialStepCount = currentStepCount
        }

        val steps = currentStepCount - initialStepCount

        sendStepCountToFragment(steps)
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val x = event.values[0].toDouble()
        val y = event.values[1].toDouble()
        val z = event.values[2].toDouble()

        val magnitude = sqrt(x * x + y * y + z * z)
        val delta = magnitude - previousMagnitude
        previousMagnitude = magnitude

        if (delta > threshold) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastStepTime > stepInterval) {
                lastStepTime = currentTime
                stepCount++
                sendStepCountToFragment(stepCount)
            }
        }

        val startTime = ZonedDateTime.now()
        val endTime = startTime.plusMinutes(1)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                healthConnectManager.writeStepsInput(startTime, endTime, stepCount.toLong())
            } catch (e: Exception) {
                Log.e(TAG, "Error writing steps data: ${e.message}")
            }
        }
    }

    private fun sendStepCountToFragment(stepCount: Int) {
        val intent = Intent("vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE")
        intent.putExtra("step_count", stepCount)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d(TAG, "Sending broadcast: step_count = $stepCount")
        NotificationsHelper.updateNotification(this, stepCount)
    }

    private fun startAsForegroundService() {
        NotificationsHelper.createNotificationChannel(this)
        val notification = NotificationsHelper.buildNotification(this, _stepCountFlow.value)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun checkForNewDay() {
        val currentDate = LocalDate.now()
        if (currentDate.isAfter(lastRecordedDate)) {
            Log.d(TAG, "New day detected! Resetting step count.")
            lastRecordedDate = currentDate
            stepCount = 0
            initialStepCount = -1
            _stepCountFlow.value = 0
            sendStepCountToFragment(stepCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
