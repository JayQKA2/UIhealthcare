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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import vn.edu.usth.uihealthcare.R
import java.text.SimpleDateFormat
import java.util.*

class StepsActivity : AppCompatActivity() {

    private lateinit var stepsTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var toolbar: Toolbar
    private lateinit var barChart: BarChart
    private var currentSteps = 0
    private val stepsData = mutableListOf<BarEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        stepsTextView = findViewById(R.id.steps_value)
        calendarView = findViewById(R.id.calendar)
        toolbar = findViewById(R.id.toolbar1)
        barChart = findViewById(R.id.chart1)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val filter = IntentFilter("vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE")
        LocalBroadcastManager.getInstance(this).registerReceiver(stepReceiver, filter)

        calendarView.setOnDateChangeListener { _, _, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d", dayOfMonth, month + 1)
            stepsTextView.text = "$selectedDate: $currentSteps steps"
        }

        Checkpermission()
    }

    private fun Checkpermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("CAMERA", "Permission granted! ")
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100)
            }
        }
    }

    private val stepReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("StepsActivity", "Received broadcast")
            if (intent?.action == "vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE") {
                val stepCount = intent.getIntExtra("step_count", 0)
                Log.d("StepsAC", "onReceive: $stepCount ")
                updateUI(stepCount)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(steps: Int) {
        currentSteps = steps
        stepsTextView.text = "$steps steps"
        Log.d("StepsAC", "updateUI: $steps ")
        updateChart(steps)
    }

    private fun updateChart(steps: Int) {
        val time = stepsData.size.toFloat()
        stepsData.add(BarEntry(time, steps.toFloat()))
        val barDataSet = BarDataSet(stepsData, "Step Count")
        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.invalidate()
    }

    private fun sendStepUpdate(stepCount: Int) {
        val intent = Intent("vn.edu.usth.uihealthcare.STEP_UPDATE")
        intent.putExtra("step_count", stepCount)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stepReceiver)

    }
}