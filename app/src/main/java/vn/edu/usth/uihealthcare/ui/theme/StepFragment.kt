package vn.edu.usth.uihealthcare.ui.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import org.json.JSONArray
import org.json.JSONObject
import vn.edu.usth.uihealthcare.R
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StepFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var stepsData: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var barEntries: ArrayList<BarEntry>
    private lateinit var dates: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_step, container, false)

        barChart = view.findViewById<BarChart>(R.id.chart1)
        stepsData = view.findViewById<TextView>(R.id.data)
        calendarView = view.findViewById<CalendarView>(R.id.calendar)

        val jsonData = loadJSONFromAsset()
        setupChartData(jsonData)

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
        triggerCurrentDate()

        return view
    }

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

    private fun highlightBar(index: Int) {
        barChart.highlightValue(index.toFloat(), 0)
        barChart.invalidate()
    }

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