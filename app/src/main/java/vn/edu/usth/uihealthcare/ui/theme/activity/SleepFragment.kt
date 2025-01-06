package vn.edu.usth.uihealthcare.ui.theme.activity

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class SleepFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var healthConnectManager: HealthConnectManager
    private var sleepStartTime: LocalDateTime? = null
    private var sleepEndTime: LocalDateTime? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        val timeDisplay: TextView = view.findViewById(R.id.time_display)
        val weeklyCheckCard: CardView = view.findViewById(R.id.weekly_check_card)

        val sleepStartButton: Button = view.findViewById(R.id.sleep_start_button)
        val sleepEndButton: Button = view.findViewById(R.id.sleep_end_button)
        val saveSleepButton: Button = view.findViewById(R.id.save_sleep_button)

        healthConnectManager = HealthConnectManager(requireContext())

        val updateTime = object : Runnable {
            @SuppressLint("DefaultLocale")
            override fun run() {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)
                val formattedTime = String.format("%02d:%02d:%02d", hour, minute, second)

                timeDisplay.text = formattedTime
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateTime)

        sleepStartButton.setOnClickListener {
            showTimePicker { time ->
                sleepStartTime = time
                sleepStartButton.text = "Start: ${time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
            }
        }

        sleepEndButton.setOnClickListener {
            showTimePicker { time ->
                sleepEndTime = time
                sleepEndButton.text = "End: ${time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
            }
        }

        saveSleepButton.setOnClickListener {
            if (sleepStartTime != null && sleepEndTime != null && sleepEndTime!!.isAfter(sleepStartTime)) {
                lifecycleScope.launch {
                    try {
                        healthConnectManager.writeSleepSession(
                            healthConnectClient = HealthConnectClient.getOrCreate(requireContext()),
                            start = sleepStartTime!!.atZone(ZoneId.systemDefault()),
                            end = sleepEndTime!!.atZone(ZoneId.systemDefault())
                        )
                        Toast.makeText(requireContext(), "Sleep session saved successfully.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Failed to save sleep session: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please select valid start and end times.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showTimePicker(onTimeSelected: (LocalDateTime) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val now = LocalDateTime.now()
                val time = now.withHour(hourOfDay).withMinute(minute).withSecond(0).withNano(0)
                onTimeSelected(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}
