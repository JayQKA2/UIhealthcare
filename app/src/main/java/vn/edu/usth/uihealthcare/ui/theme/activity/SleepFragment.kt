package vn.edu.usth.uihealthcare.ui.theme.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar

class SleepFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var healthConnectManager: HealthConnectManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        val timeDisplay: TextView = view.findViewById(R.id.time_display)
        val weeklyCheckCard: CardView = view.findViewById(R.id.weekly_check_card)
        val weightInputEditText: EditText = view.findViewById(R.id.weight_input_edit_text)
        val saveWeightButton: Button = view.findViewById(R.id.save_weight_button)
        val sleepStartInput: EditText = view.findViewById(R.id.sleep_start_input)
        val sleepEndInput: EditText = view.findViewById(R.id.sleep_end_input)
        val saveSleepButton: Button = view.findViewById(R.id.save_sleep_button)

        healthConnectManager = HealthConnectManager(requireContext())

        // Update current time every second
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

        // Save weight button logic
        saveWeightButton.setOnClickListener {
            val weightInputText = weightInputEditText.text.toString()
            if (weightInputText.isNotEmpty()) {
                val weight = weightInputText.toDouble()
                lifecycleScope.launch {
                    try {
                        healthConnectManager.writeWeightInput(weight)
                        Toast.makeText(requireContext(), "Weight saved successfully.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Failed to save weight: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show()
            }
        }

        // Save sleep button logic
        saveSleepButton.setOnClickListener {
            val startTimeText = sleepStartInput.text.toString()
            val endTimeText = sleepEndInput.text.toString()

            if (startTimeText.isNotEmpty() && endTimeText.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        val startDateTime = LocalDateTime.parse(startTimeText, formatter)
                            .atZone(ZoneId.systemDefault())
                        val endDateTime = LocalDateTime.parse(endTimeText, formatter)
                            .atZone(ZoneId.systemDefault())

                        if (endDateTime.isAfter(startDateTime)) {
                            healthConnectManager.writeSleepSession(
                                healthConnectClient = HealthConnectClient.getOrCreate(requireContext()),
                                start = startDateTime,
                                end = endDateTime
                            )
                            Toast.makeText(requireContext(), "Sleep session saved successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "End time must be after start time.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Failed to save sleep session: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please provide both start and end times.", Toast.LENGTH_SHORT).show()
            }
        }



        // Weekly check navigation logic
        weeklyCheckCard.setOnClickListener { v: View ->
            val navController: NavController = Navigation.findNavController(v)
            navController.navigate(R.id.action_sleep2)
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
