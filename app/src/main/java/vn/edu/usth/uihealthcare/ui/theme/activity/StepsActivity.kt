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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.CalendarView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.sensor.StepsSensorService
import vn.edu.usth.uihealthcare.Data.Step
import java.text.SimpleDateFormat
import java.util.*

class StepsActivity : AppCompatActivity() {

    private lateinit var stepsTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var sensorReceiver: BroadcastReceiver
    private lateinit var toolbar: Toolbar
    private lateinit var barChart: BarChart
    private var currentSteps = 0
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1 second
    private val stepsList = mutableListOf<Step>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        stepsTextView = findViewById(R.id.steps_value)
        calendarView = findViewById(R.id.calendar)
        toolbar = findViewById(R.id.toolbar1)
        barChart = findViewById(R.id.chart1)

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
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            stepsTextView.text = "$selectedDate: $currentSteps steps"
            stepsList.add(Step(selectedDate, currentSteps, currentTime))
            updateChart()
        }

        checkPermission()
        startStepSensorService()

        // Start periodic UI update
        handler.post(updateRunnable)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateUI(getStepData())
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật UI khi Activity được mở lại
        updateUI(getStepData())
    }

    private fun saveStepData(steps: Int) {
        val sharedPreferences = getSharedPreferences("step_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("steps", steps)
        editor.apply()
        Log.d("StepsActivity", "Saved steps: $steps")
    }

    private fun getStepData(): Int {
        val sharedPreferences = getSharedPreferences("step_data", Context.MODE_PRIVATE)
        val steps = sharedPreferences.getInt("steps", 0)
        Log.d("StepsActivity", "Retrieved steps: $steps")
        return steps
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

    private fun updateChart() {
        val entries = stepsList.mapIndexed { index, step -> BarEntry(index.toFloat(), step.count.toFloat()) }
        val dataSet = BarDataSet(entries, "Steps")
        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate() // refresh the chart
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sensorReceiver)
        handler.removeCallbacks(updateRunnable)
    }
}