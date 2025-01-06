package vn.edu.usth.uihealthcare.ui.theme

import vn.edu.usth.uihealthcare.ui.theme.activity.SleepActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.ui.theme.activity.HeartActivity
import vn.edu.usth.uihealthcare.ui.theme.activity.StepsActivity

class HomeFragment : Fragment() {

    private lateinit var diagnosticButton: Button
    private lateinit var notificationIcon: ImageButton
    private val TAG = "home"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        notificationIcon = view.findViewById(R.id.notificationIcon)
        diagnosticButton = view.findViewById(R.id.diagnosticButton)

        diagnosticButton.setOnClickListener { v: View ->
            val navController: NavController = Navigation.findNavController(v)
            navController.navigate(R.id.action_test)
        }

        notificationIcon.setOnClickListener {
            Log.d(TAG, "Notifications")
        }

        view.findViewById<View>(R.id.steps).setOnClickListener {
            val intent = Intent(context, StepsActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.heart_pressure).setOnClickListener {
            val intent2 = Intent(context, HeartActivity::class.java)
            startActivity(intent2)
        }

        view.findViewById<View>(R.id.sleepCard).setOnClickListener {
            val intent3 = Intent(context, SleepActivity::class.java)
            startActivity(intent3)
        }

        return view
    }
}