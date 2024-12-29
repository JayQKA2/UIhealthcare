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
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.Data.DataProcessor.DataProcessor
import vn.edu.usth.uihealthcare.Data.Entity.WeightEntity
import vn.edu.usth.uihealthcare.Data.ViewModel.WeightViewModel
import vn.edu.usth.uihealthcare.Data.ViewModel.WeightViewModelFactory
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.Instant
import java.time.ZoneOffset
import java.util.Calendar

class SleepFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private var healthConnectClient: HealthConnectClient? = null

    private val weightViewModel: WeightViewModel by viewModels {
        WeightViewModelFactory(HealthConnectManager(requireContext()))
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        val timeDisplay: TextView = view.findViewById(R.id.time_display)
        val weeklyCheckCard: CardView = view.findViewById(R.id.weekly_check_card)
        val weightInputEditText: EditText = view.findViewById(R.id.weight_input_edit_text)
        val saveWeightButton: Button = view.findViewById(R.id.save_weight_button)

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
                    saveWeight(weight)
                } else {
                    Toast.makeText(context, "Please enter a valid weight.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Weight field cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

//    private fun saveWeightToHealthConnect(weight: Double) {
//        lifecycleScope.launch {
//            try {
//                // Save weight to Health Connect
//                val now = Instant.now()
//                val weightRecord = WeightRecord(
//                    weight = Mass.kilograms(weight),
//                    time = now,
//                    zoneOffset = ZoneOffset.UTC
//                )
//                healthConnectClient?.insertRecords(listOf(weightRecord))
//                Log.d("SleepFragment", "WeightRecord inserted: $weightRecord")
//
//                // Save weight to Room database
//                val weightEntity = WeightEntity(weight = weight.toFloat(), timestamp = now.toEpochMilli())
//                weightViewModel.insertWeight(weightEntity)
//                Log.d("SleepFragment", "WeightEntity inserted: $weightEntity")
//
//////                Toast.makeText(context, "Weight saved: $weight kg", Toast.LENGTH_SHORT).show()
//////                view?.findViewById<Button>(R.id.save_weight_button)?.text = "Weight Saved"
////
////                view?.findViewById<TextView>(R.id.weight_display)?.text = "Weight: $weight kg"
////                Toast.makeText(context, "Weight saved: $weight kg", Toast.LENGTH_SHORT).show()
////
//////                context?.let {
//////                    Toast.makeText(it, "Weight saved: $weight kg", Toast.LENGTH_SHORT).show()
//////                    view?.findViewById<Button>(R.id.save_weight_button)?.text ="@+id/weight_display"
//////                }
//            } catch (e: Exception) {
//                Log.e("SleepFragment", "Failed to save weight: ${e.message}")
//                Toast.makeText(context, "Lưu thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    private fun saveWeight(weight: Double) {
        lifecycleScope.launch {
            context?.let {
                DataProcessor.processWeightData(it, weight, healthConnectClient, weightViewModel)
                view?.findViewById<TextView>(R.id.weight_display)?.text = "Weight: $weight kg"
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}