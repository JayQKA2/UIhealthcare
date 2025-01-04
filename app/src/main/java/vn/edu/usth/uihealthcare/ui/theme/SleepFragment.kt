package vn.edu.usth.uihealthcare.ui.theme

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
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar

class SleepFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private var healthConnectClient: HealthConnectClient? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        val timeDisplay: TextView = view.findViewById(R.id.time_display)
        val weeklyCheckCard: CardView = view.findViewById(R.id.weekly_check_card)
        val weightInputEditText: EditText = view.findViewById(R.id.weight_input_edit_text)
        val saveWeightButton: Button = view.findViewById(R.id.save_weight_button)
        val sleepStartInput: EditText = view.findViewById(R.id.sleep_start_input)
        val sleepEndInput: EditText = view.findViewById(R.id.sleep_end_input)
        val saveSleepButton: Button = view.findViewById(R.id.save_sleep_button)

        healthConnectClient = HealthConnectClient.getOrCreate(requireContext())

        val updateTime = object : Runnable {
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

        weeklyCheckCard.setOnClickListener { v: View ->
            val navController: NavController = Navigation.findNavController(v)
            navController.navigate(R.id.action_sleep2)
        }

        saveWeightButton.setOnClickListener {
            val weightInput = weightInputEditText.text.toString()
            if (weightInput.isNotEmpty()) {
                val weight = weightInput.toDoubleOrNull()
                if (weight != null && weight > 0) {
                    saveWeightToHealthConnect(weight)
                } else {
                    Toast.makeText(context, "Please enter a valid weight.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Weight field cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        saveSleepButton.setOnClickListener {
            val sleepStart = sleepStartInput.text.toString()
            val sleepEnd = sleepEndInput.text.toString()
            if (sleepStart.isNotEmpty() && sleepEnd.isNotEmpty()) {
                saveSleepToHealthConnect(sleepStart, sleepEnd)
            } else {
                Toast.makeText(context, "Both sleep start and end times must be provided.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun saveWeightToHealthConnect(weight: Double) {
        lifecycleScope.launch {
            try {
                val now = Instant.now()
                val weightRecord = WeightRecord(
                    weight = Mass.kilograms(weight),
                    time = now,
                    zoneOffset = ZoneOffset.UTC
                )
                healthConnectClient?.insertRecords(listOf(weightRecord))

                Toast.makeText(context, "Weight saved: $weight kg", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SleepFragment", "Failed to save weight: ${e.message}")
                Toast.makeText(context, "Failed to save weight: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSleepToHealthConnect(startTime: String, endTime: String) {
        lifecycleScope.launch {
            try {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val startDateTime = LocalDateTime.parse(startTime, formatter).atZone(ZoneId.systemDefault()).toInstant()
                val endDateTime = LocalDateTime.parse(endTime, formatter).atZone(ZoneId.systemDefault()).toInstant()

                if (endDateTime.isAfter(startDateTime)) {
                    val sleepRecord = SleepSessionRecord(
                        startTime = startDateTime,
                        endTime = endDateTime,
                        startZoneOffset = ZoneOffset.UTC, // Add this
                        endZoneOffset = ZoneOffset.UTC, // Add this
                        title = "Night Sleep"
                    )

                    healthConnectClient?.insertRecords(listOf(sleepRecord))

                    Toast.makeText(context, "Sleep record saved.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "End time must be after start time.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("SleepFragment", "Failed to save sleep: ${e.message}")
                Toast.makeText(context, "Failed to save sleep: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
