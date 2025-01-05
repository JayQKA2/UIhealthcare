package vn.edu.usth.uihealthcare.ui.theme.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.sensor.StepsSensorService

class StepsActivity : AppCompatActivity() {

    private lateinit var stepsTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var sensorReceiver: BroadcastReceiver
    private lateinit var toolbar: Toolbar
    private var currentSteps = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        stepsTextView = findViewById(R.id.steps_value)
        calendarView = findViewById(R.id.calendar)
        toolbar = findViewById(R.id.toolbar1)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        checkPermission()

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        sensorReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE") {
                    val stepCount = intent.getIntExtra("step_count", 0)
                    updateUI(stepCount)
                }
            }
        }

        val filter = IntentFilter("vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE")
        registerReceiver(sensorReceiver, filter, RECEIVER_NOT_EXPORTED)

        calendarView.setOnDateChangeListener { _, _, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d", dayOfMonth, month + 1)
            stepsTextView.text = "$selectedDate: $currentSteps steps"
        }

        checkPermission()
        startStepSensorService()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("ACTIVITY_RECOGNITION", "Permission granted! ")
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100)
            }
        }
    }

    private fun startStepSensorService() {
        val serviceIntent = Intent(this, StepsSensorService::class.java)
        startService(serviceIntent)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(steps: Int) {
        currentSteps = steps
        stepsTextView.text = "$steps steps"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sensorReceiver)
    }
}
