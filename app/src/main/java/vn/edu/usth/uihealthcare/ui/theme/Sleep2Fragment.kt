package vn.edu.usth.uihealthcare.ui.theme

import android.annotation.SuppressLint
import android.health.connect.datatypes.units.Mass
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.Instant

class Sleep2Fragment : Fragment() {

    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var weightTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sleep2, container, false)

        weightTextView = view.findViewById(R.id.data)

        healthConnectManager = HealthConnectManager(requireContext())

        fetchWeightData()

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun fetchWeightData() {
        lifecycleScope.launch {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minusSeconds(60 * 60 * 24)

                val weightRecords = healthConnectManager.readWeightInputs(startTime, endTime)

                if (weightRecords.isNotEmpty()) {
                    val latestWeight = weightRecords.first()

                    val mass = latestWeight.weight.inKilograms

                    weightTextView.text = "Weight: $mass kg"
                } else {
                    weightTextView.text = "No weight data found."
                }
            } catch (e: Exception) {
                weightTextView.text = "Failed to fetch weight data: ${e.message}"
            }
        }
    }


}
