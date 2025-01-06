package vn.edu.usth.uihealthcare.ui.theme.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.joery.timerangepicker.TimeRangePicker
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SleepActivity : AppCompatActivity() {

    private lateinit var sleepTextView: TextView
    private var timeTextView: TextView? = null
    private lateinit var durationTextView: TextView
    private lateinit var toolbar: Toolbar
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var healthConnectManager: HealthConnectManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep)

        val picker: TimeRangePicker = findViewById(R.id.picker)
        startTimeTextView = findViewById(R.id.start_time)
        endTimeTextView = findViewById(R.id.end_time)

        sleepTextView = findViewById(R.id.data)
        timeTextView = findViewById(R.id.date)
        durationTextView = findViewById(R.id.duration)

        val saveSleepButton: Button = findViewById(R.id.save_sleep_button)
        toolbar = findViewById(R.id.toolbar3)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        healthConnectManager = HealthConnectManager(this)


        picker.setOnTimeChangeListener(object : TimeRangePicker.OnTimeChangeListener {
            override fun onStartTimeChange(startTime: TimeRangePicker.Time) {
                startTimeTextView.text = formatTime(startTime.hour, startTime.minute)
            }

            override fun onEndTimeChange(endTime: TimeRangePicker.Time) {
                endTimeTextView.text = formatTime(endTime.hour, endTime.minute)
            }

            override fun onDurationChange(duration: TimeRangePicker.TimeDuration) {
                Log.d("TimeRangePicker", "Duration: ${duration.hour} hours ${duration.minute} minutes")
            }
        })

        saveSleepButton.setOnClickListener {
            val sleepTime = startTimeTextView.text.toString()
            val wakeTime = endTimeTextView.text.toString()

            if (sleepTime.isEmpty() || wakeTime.isEmpty()) {
                Toast.makeText(this, "Please enter both sleep and wake times", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Saved Sleep: $sleepTime - $wakeTime", Toast.LENGTH_SHORT).show()

            val startZonedDateTime = convertToZonedDateTime(sleepTime)
            val endZonedDateTime = convertToZonedDateTime(wakeTime)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    healthConnectManager.writeSleepSession(healthConnectManager.healthConnectClient, startZonedDateTime, endZonedDateTime)
                } catch (e: Exception) {
                    Log.e("vn.edu.usth.uihealthcare.ui.theme.activity.SleepActivity", "Error saving sleep data", e)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    private fun convertToZonedDateTime(time: String): ZonedDateTime {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val localTime = java.time.LocalTime.parse(time, formatter)
        return ZonedDateTime.of(localTime.atDate(java.time.LocalDate.now()), ZoneId.systemDefault())
    }

    @SuppressLint("SetTextI18n")
    private fun fetchSleep() {
        lifecycleScope.launch {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minusSeconds(60 * 60 * 24)

                val sleepSessionRecord = healthConnectManager.readSleepSession(startTime, endTime)
                if (sleepSessionRecord.isNotEmpty()) {
                    val latestsleeping = sleepSessionRecord.first()

                    val start = latestsleeping.startTime
                    val end = latestsleeping.endTime

                    timeTextView!!.text = "From: $start to $end"

                } else {
                    timeTextView!!.text = "No sleep data found."
                }
            } catch (e: Exception) {
                timeTextView!!.text = "Failed to fetch sleep data: ${e.message}"
            }
        }
    }

}
