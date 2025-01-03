package vn.edu.usth.uihealthcare.ui.theme

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import vn.edu.usth.uihealthcare.R
import java.util.Calendar

class Sleep2Fragment : Fragment() {

    private lateinit var tvSleepTime: TextView
    private lateinit var btnSetSleepTime: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sleep2, container, false)

        tvSleepTime = view.findViewById(R.id.tvSleepTime)
        btnSetSleepTime = view.findViewById(R.id.btnSetSleepTime)

        btnSetSleepTime.setOnClickListener {
            // Lấy thời gian hiện tại
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Hiển thị TimePickerDialog
            val timePickerDialog = TimePickerDialog(requireContext(),
                { _, selectedHour, selectedMinute ->
                    // Định dạng giờ
                    val sleepTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    tvSleepTime.text = "Giờ đi ngủ: $sleepTime"
                }, hour, minute, true)
            timePickerDialog.show()
        }

        return view
    }

}
