package vn.edu.usth.uihealthcare.ui.theme

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import vn.edu.usth.uihealthcare.R
import java.util.Calendar

class SleepFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        val timeDisplay: TextView = view.findViewById(R.id.time_display)

        // Runnable cập nhật thời gian mỗi giây
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

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Xóa tất cả callback khi Fragment bị hủy
    }
}