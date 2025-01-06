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
import androidx.cardview.widget.CardView
import nl.joery.timerangepicker.TimeRangePicker
import vn.edu.usth.uihealthcare.R
import java.util.*

class SleepActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView

    private val updateTime = object : Runnable {
        @SuppressLint("DefaultLocale")
        override fun run() {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val formattedTime = String.format("%02d:%02d:%02d", hour, minute, second)

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep)

        val picker: TimeRangePicker = findViewById(R.id.picker)
        startTimeTextView = findViewById(R.id.start_time)
        endTimeTextView = findViewById(R.id.end_time)
        val saveSleepButton: Button = findViewById(R.id.save_sleep_button)
        val weeklyCheckCard: CardView = findViewById(R.id.weekly_check_card)

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

            fun onDragStart(thumb: TimeRangePicker.Thumb): Boolean {
                return true
            }

            fun onDragStop(thumb: TimeRangePicker.Thumb) {
            }
        })

        saveSleepButton.setOnClickListener {
            val sleepTime = startTimeTextView.text.toString()
            val wakeTime = endTimeTextView.text.toString()
            Toast.makeText(
                this,
                "Saved Sleep: $sleepTime - $wakeTime",
                Toast.LENGTH_SHORT
            ).show()
        }

        weeklyCheckCard.setOnClickListener {
            // Handle weekly check card click (no navigation here since it's an Activity)
            Toast.makeText(this, "Weekly check clicked!", Toast.LENGTH_SHORT).show()
        }

        handler.post(updateTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

}
