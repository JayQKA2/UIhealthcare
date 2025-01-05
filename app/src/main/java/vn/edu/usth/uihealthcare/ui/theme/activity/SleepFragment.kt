package vn.edu.usth.uihealthcare.ui.theme.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
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
//        val weightInputEditText: EditText = view.findViewById(R.id.weight_input_edit_text)
//        val saveWeightButton: Button = view.findViewById(R.id.save_weight_button)

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

//        saveWeightButton.setOnClickListener {
//            val weightInputText = weightInputEditText.text.toString()
//            if (weightInputText.isNotEmpty()) {
//                val weight = weightInputText.toDouble()
//                lifecycleScope.launch {
//                    try {
//                        healthConnectManager.writeWeightInput(weight)
//                    } catch (e: Exception) {
//                        Toast.makeText(requireContext(), "Failed to save weight: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } else {
//                Toast.makeText(requireContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show()
//            }
//        }

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