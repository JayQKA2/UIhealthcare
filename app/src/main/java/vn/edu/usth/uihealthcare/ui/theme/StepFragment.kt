package vn.edu.usth.uihealthcare.ui.theme

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.sensor.StepsSensorService

class StepFragment : Fragment() {

    private lateinit var stepsTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var sensorReceiver: BroadcastReceiver
    private var currentSteps = 0

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_step, container, false)

        stepsTextView = view.findViewById(R.id.steps_value)
        calendarView = view.findViewById(R.id.calendar)

        sensorReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE") {
                    val stepCount = intent.getIntExtra("step_count", 0)
                    updateUI(stepCount)
                }
            }
        }

        val filter = IntentFilter("vn.edu.usth.uihealthcare.STEP_COUNT_UPDATE")
        requireContext().registerReceiver(sensorReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d", dayOfMonth, month + 1)
            stepsTextView.text = "$selectedDate: $currentSteps steps"
        }

        startStepSensorService()

        return view
    }


    @SuppressLint("SetTextI18n")
    private fun updateUI(steps: Int) {
        currentSteps = steps
        stepsTextView.text = "$steps steps"
    }

    private fun startStepSensorService() {
        val serviceIntent = Intent(requireContext(), StepsSensorService::class.java)
        requireContext().startService(serviceIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(sensorReceiver)
    }
}
