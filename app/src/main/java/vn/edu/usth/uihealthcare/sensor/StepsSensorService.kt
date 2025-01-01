package vn.edu.usth.uihealthcare.sensor

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
    private val threshold = 10.0
    private var stepCount = 0
    private var isHaveStepCounter = true

    companion object {
        private const val TAG = "MM_StepsSensorService"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null) {
            isHaveStepCounter = false
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isHaveStepCounter) {
            calculatorStepCounter(event)
        } else {
            calculatorAccelerometer(event)
        }
    }

    private fun calculatorStepCounter(event: SensorEvent) {
        val eventSteps = event.values[0].toInt()
        if (steps == 0) {
            steps = eventSteps
        } else {
            val deltaSteps = eventSteps - steps
            steps = eventSteps
            sendStepCountToFragment(steps)
            Log.e(TAG, "calculatorStepCounter: "+ steps )
        }
    }

    private fun sendStepCountToFragment(stepCount: Int) {
        val intent = Intent("vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE")
        intent.putExtra("step_count", stepCount)
        sendBroadcast(intent)
    }

    private fun calculatorAccelerometer(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()

            val magnitude = sqrt(x * x + y * y + z * z)
            val delta = magnitude - previousMagnitude
            previousMagnitude = magnitude

            if (delta > threshold) {
                stepCount++
                sendStepCountToFragment(stepCount)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}
