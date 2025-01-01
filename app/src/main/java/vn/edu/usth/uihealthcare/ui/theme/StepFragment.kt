package vn.edu.usth.uihealthcare.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.feature.ExperimentalFeatureAvailabilityApi
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

class StepFragment : Fragment() {

    private var healthConnectClient: HealthConnectClient? = null
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var barChart: BarChart
    private lateinit var stepsData: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var barEntries: ArrayList<BarEntry>
    private lateinit var dates: ArrayList<String>
    private lateinit var stepsTextView: TextView
    private lateinit var caloriesTextView: TextView

    private var currentSteps: Int = 0
    private var stepCounter: Long = 0

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalFeatureAvailabilityApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_step, container, false)

        healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
        healthConnectManager = HealthConnectManager(requireContext())

        stepsTextView = view.findViewById(R.id.steps_title)
        caloriesTextView = view.findViewById(R.id.calor_value)

        barChart = view.findViewById<BarChart>(R.id.chart1)
        stepsData = view.findViewById<TextView>(R.id.data)
        calendarView = view.findViewById<CalendarView>(R.id.calendar)

        val jsonData = loadJSONFromAsset()
        setupChartData(jsonData)

        // Set up CalendarView date change listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format(Locale.getDefault(), "%02d/%02d", dayOfMonth, month + 1)
            var found = false

            for (i in dates.indices) {
                if (dates[i] == selectedDate) {
                    stepsData.text = String.format("%s steps", barEntries[i].y.toInt())
                    highlightBar(i)
                    found = true
                    break
                }
            }

            if (!found) {
                stepsData.text = "No data for this date"
            }
        }

        // Check if HealthConnect features are available
        backgroundReadAvailable()
        triggerCurrentDate()
        recordSteps()

        return view
    }

    // Check if reading health data in background is available
    @OptIn(ExperimentalFeatureAvailabilityApi::class)
    private fun backgroundReadAvailable() {
        healthConnectManager.isFeatureAvailable(
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
        )
    }

    // Fetch step data
    private fun fetchStepData(interval: Long) {
        lifecycleScope.launch {
            try {
                val stepsRecords = healthConnectManager.readStepsRecords(interval)
                val totalSteps = stepsRecords.sumOf { it.metricValue.toInt() }

                currentSteps += totalSteps
                updateUI(currentSteps)

                for (record in stepsRecords) {
                    Log.d("StepData", "From: ${record.fromDatetime}, To: ${record.toDatetime}, Steps: ${record.metricValue}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to fetch step data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("fetchStepData", "Error: ${e.message}", e)
            }
        }
    }

    // Get step counter value from SharedPreferences
    private fun getStepCounterFromPreferences(): Long {
        val sharedPref = requireContext().getSharedPreferences("StepPreferences", Context.MODE_PRIVATE)
        return sharedPref.getLong("stepCounter", 0)
    }

    // Save step counter value to SharedPreferences
    private fun saveStepCounterToPreferences(stepCounter: Long) {
        val sharedPref = requireContext().getSharedPreferences("StepPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("stepCounter", stepCounter)
            apply()
        }
    }

    // Record steps in the background
    private fun recordSteps() {
        lifecycleScope.launch {
            try {
                val now = ZonedDateTime.now(ZoneId.systemDefault())
                val start = now
                val end = now.plusMinutes(1)

                if (start.isBefore(end)) {
                    stepCounter = getStepCounterFromPreferences() // Retrieve saved step counter
                    stepCounter++ // Increment step counter

                    // Save the updated step counter
                    saveStepCounterToPreferences(stepCounter)

                    // Ensure that the recorded steps are at least 1
                    val stepsToRecord = if (stepCounter > 0) stepCounter else 1

                    healthConnectManager.writeStepsInput(start, end, count = stepsToRecord)
                    Toast.makeText(context, "Step session recorded", Toast.LENGTH_SHORT).show()
                    Log.d("haha", "recordSteps: " + stepCounter)

                    fetchStepData(interval = 1) // Fetch step data after recording
                } else {
                    Toast.makeText(context, "Invalid time range for steps", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Failed to record steps: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("TAG", "recordSteps: " + e.message)
            }
        }
    }

    // Calculate calories burned based on steps
    private fun calculateCalories(steps: Int): Int {
        val caloriesPerStep = 0.04 // Example: 1 step = 0.04 calories
        return (steps * caloriesPerStep).toInt()
    }

    // Update UI with the current steps and calories
    @SuppressLint("SetTextI18n")
    private fun updateUI(steps: Int) {
        stepsTextView.text = "$steps steps"
        val caloriesBurned = calculateCalories(steps)
        caloriesTextView.text = "$caloriesBurned Calories"
    }

    // Trigger current date and update chart data accordingly
    @SuppressLint("SetTextI18n")
    private fun triggerCurrentDate() {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTime }
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)

        calendarView.setDate(currentTime, false, true)

        val currentDate = String.format(Locale.getDefault(), "%02d/%02d", currentDay, currentMonth + 1)
        var found = false

        for (i in dates.indices) {
            if (dates[i] == currentDate) {
                stepsData.text = String.format("%s steps", barEntries[i].y.toInt())
                highlightBar(i)
                found = true
                break
            }
        }

        if (!found) {
            stepsData.text = "No data for this date"
        }
    }

    // Set up chart data from JSON
    private fun setupChartData(jsonData: String?) {
        try {
            barEntries = ArrayList()
            dates = ArrayList()
            val jsonObject = JSONObject(jsonData!!)
            val dailyData: JSONArray = jsonObject.getJSONArray("daily_data")

            for (i in 0 until dailyData.length()) {
                val data = dailyData.getJSONObject(i)
                val timestamp = data.getLong("date")
                val steps = data.getInt("steps")

                val date = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp * 1000))
                dates.add(date)
                barEntries.add(BarEntry(i.toFloat(), steps.toFloat()))
            }

            val barDataSet = BarDataSet(barEntries, "Steps").apply {
                setColor(resources.getColor(android.R.color.holo_blue_dark, null))
                setDrawValues(false)
            }

            barChart.apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.apply {
                    setDrawLabels(false)
                    setDrawAxisLine(false)
                    setDrawGridLines(false)
                }
                axisRight.isEnabled = false
                data = BarData(barDataSet)
                invalidate()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Highlight a bar in the chart based on index
    private fun highlightBar(index: Int) {
        barChart.highlightValue(index.toFloat(), 0)
        barChart.invalidate()
    }

    // Load JSON data from assets
    private fun loadJSONFromAsset(): String? {
        return try {
            val inputStream: InputStream = requireContext().assets.open("simple.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
