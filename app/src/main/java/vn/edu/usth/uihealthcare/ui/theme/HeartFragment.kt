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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_heart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animationView = view.findViewById(R.id.animationView)
        heartRateTextView = view.findViewById(R.id.num_heart)
        animationView.visibility = View.VISIBLE
    }
}
