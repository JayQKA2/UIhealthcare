package vn.edu.usth.uihealthcare.sensor
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

    companion object {
        private const val TAG = "MM_StepsSensorService"
        private const val CHANNEL_ID = "steps_sensor_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null) {
            isHaveStepCounter = false
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d(TAG, "Service started and sensor registered.")

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
            val deltaSteps = eventSteps - steps
            steps = eventSteps
            sendStepCountToFragment(steps)
            Log.d(TAG, "Step Counter: Steps detected = $deltaSteps, Total = $steps")
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
                Log.d(TAG, "Accelerometer: Step detected. Total steps = $stepCount")
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

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Service stopped and sensor unregistered.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
