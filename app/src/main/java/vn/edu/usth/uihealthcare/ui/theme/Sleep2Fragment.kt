package vn.edu.usth.uihealthcare.ui.theme

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import vn.edu.usth.uihealthcare.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Sleep2Fragment : Fragment() {

    private lateinit var tvSleepTime: TextView
    private lateinit var btnSetSleepTime: Button
    private lateinit var timeDisplay: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val timeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // Cập nhật mỗi giây
        }
    }
    private var sleepHour: Int = 0
    private var sleepMinute: Int = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sleep2, container, false)

        tvSleepTime = view.findViewById(R.id.tvSleepTime)
        btnSetSleepTime = view.findViewById(R.id.btnSetSleepTime)
        timeDisplay = view.findViewById(R.id.time_display)

        btnSetSleepTime.setOnClickListener {
            // Lấy thời gian hiện tại
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Hiển thị TimePickerDialog
            val timePickerDialog = TimePickerDialog(requireContext(),
                { _, selectedHour, selectedMinute ->
                    sleepHour = selectedHour
                    sleepMinute = selectedMinute

                    val sleepTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    tvSleepTime.text = "Giờ đi ngủ: $sleepTime"
                    setAlarm(selectedHour, selectedMinute)
                }, hour, minute, true)
            timePickerDialog.show()
        }

        return view
    }
    private fun setAlarm(hour: Int, minute: Int) {
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, 0)

        // Thiết lập alarm
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Thiết lập alarm
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
    override fun onStart() {
        super.onStart()
        handler.post(timeRunnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(timeRunnable)
    }

    private fun updateTime() {
        val currentTime = Calendar.getInstance()
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeDisplay.text = sdf.format(currentTime.time)
    }

}
