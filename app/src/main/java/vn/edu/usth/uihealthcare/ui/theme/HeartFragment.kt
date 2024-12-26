package vn.edu.usth.uihealthcare.ui.theme

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import vn.edu.usth.uihealthcare.R

class HeartFragment : Fragment() {

    private lateinit var animationView: LottieAnimationView
    private lateinit var heartRateTextView: TextView

    // Receiver to listen for heart rate updates
    private val heartRateReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            if ("whs.synthetic.user.HEART_RATE" == intent.action) {
                val heartRate = intent.getIntExtra("heart_rate", 0)
                Log.d("HeartFragment", "Heart Rate: $heartRate")
                heartRateTextView.text = "Heart Rate: $heartRate"
                animationView.playAnimation()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_heart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize the views
        animationView = view.findViewById(R.id.animationView)
        heartRateTextView = view.findViewById(R.id.num_heart)
        animationView.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        // Register the BroadcastReceiver for heart rate updates
        Log.d("HeartFragment", "Registering heart rate receiver")
        val intentFilter = IntentFilter("whs.synthetic.user.HEART_RATE")
        requireActivity().registerReceiver(heartRateReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        // Unregister the BroadcastReceiver
        Log.d("HeartFragment", "Unregistering heart rate receiver")
        requireActivity().unregisterReceiver(heartRateReceiver)
    }
}
