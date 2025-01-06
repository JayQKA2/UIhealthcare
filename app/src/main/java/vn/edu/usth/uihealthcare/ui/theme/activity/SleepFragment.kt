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
import androidx.navigation.NavController
import androidx.navigation.Navigation
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
    private lateinit var sleepStartButton: Button
    private lateinit var sleepEndButton: Button
    private lateinit var sleepStartTimeTextView: TextView
    private lateinit var sleepEndTimeTextView: TextView
    private var startDateTime: LocalDateTime? = null
    private var endDateTime: LocalDateTime? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        val timeDisplay: TextView = view.findViewById(R.id.time_display)
        val weeklyCheckCard: CardView = view.findViewById(R.id.weekly_check_card)
        sleepStartButton = view.findViewById(R.id.sleep_start_button)
        sleepEndButton = view.findViewById(R.id.sleep_end_button)
        sleepStartTimeTextView = view.findViewById(R.id.sleep_start_time)
        sleepEndTimeTextView = view.findViewById(R.id.sleep_end_time)
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
            showTimePickerDialog { hour, minute ->
                startDateTime = LocalDateTime.now().withHour(hour).withMinute(minute)
                val formattedTime = startDateTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                sleepStartButton.text = formattedTime
                sleepStartTimeTextView.text = "Start Time: $formattedTime"
            }
        }

        sleepEndButton.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                endDateTime = LocalDateTime.now().withHour(hour).withMinute(minute)
                val formattedTime = endDateTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                sleepEndButton.text = formattedTime
                sleepEndTimeTextView.text = "End Time: $formattedTime"
            }
        }

        saveSleepButton.setOnClickListener {
            if (startDateTime != null && endDateTime != null) {
                lifecycleScope.launch {
                    try {
                        val startZonedDateTime = startDateTime!!.atZone(ZoneId.systemDefault())
                        val endZonedDateTime = endDateTime!!.atZone(ZoneId.systemDefault())

                        if (endZonedDateTime.isAfter(startZonedDateTime)) {
                            // Save sleep session
                            Toast.makeText(requireContext(), "Sleep session saved", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "End time must be after start time", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Failed to save sleep session: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please provide both start and end times.", Toast.LENGTH_SHORT).show()
            }
        }

        weeklyCheckCard.setOnClickListener { v: View ->
            val navController: NavController = Navigation.findNavController(v)
            navController.navigate(R.id.action_sleep2)
        }

        return view
    }

    private fun showTimePickerDialog(onTimeSet: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            onTimeSet(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}