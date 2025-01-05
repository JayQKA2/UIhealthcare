package vn.edu.usth.uihealthcare.ui.theme.Settings


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import vn.edu.usth.uihealthcare.R

class SettingsFragment : Fragment() {

    private val TAG = "settings"
    private lateinit var accountTextView: TextView
    private lateinit var syncTextView: TextView
    private lateinit var measurementUnitTextView: TextView
    private lateinit var notificationsTextView: TextView
    private lateinit var accessoryTextView: TextView
    private lateinit var permissionsTextView: TextView
    private lateinit var aboutAppsTextView: TextView
    private lateinit var helpTextView: TextView
    private lateinit var signOutTextView: TextView
    private lateinit var syncSwitch: SwitchCompat

    companion object {
        private const val ACTION_APP_PERMISSION = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        accountTextView = view.findViewById(R.id.cv1t1)
        syncTextView = view.findViewById(R.id.cv1t4)
        syncSwitch = view.findViewById(R.id.cv1t5)
        measurementUnitTextView = view.findViewById(R.id.cv2t1)
        notificationsTextView = view.findViewById(R.id.cv2t3)
        accessoryTextView = view.findViewById(R.id.cv2t5)
        permissionsTextView = view.findViewById(R.id.cv3t1)
        aboutAppsTextView = view.findViewById(R.id.cv3t3)
        helpTextView = view.findViewById(R.id.cv3t5)
        signOutTextView = view.findViewById(R.id.cv4t1)

        accountTextView.setOnClickListener { showToast("Account Clicked") }
        syncTextView.setOnClickListener { showToast("Sync with Cloud Clicked") }
        measurementUnitTextView.setOnClickListener {
            Log.d(TAG, "Measure")
            openMeasurement()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationsTextView.setOnClickListener { openNotificationSettings() }
        }
        accessoryTextView.setOnClickListener { showToast("Accessory Clicked") }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionsTextView.setOnClickListener { openPermissionSettings() }
        }

        aboutAppsTextView.setOnClickListener { showToast("About Apps Clicked") }
        helpTextView.setOnClickListener { showToast("Help Clicked") }
        signOutTextView.setOnClickListener { showToast("Sign Out Clicked") }

        syncSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Sync Enabled" else "Sync Disabled"
            showToast(message)
        }

        return view
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
            }
        } else {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra("app_package", requireActivity().packageName)
                putExtra("app_uid", requireActivity().applicationInfo.uid)
            }
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Disallow!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }

    private fun openMeasurement() {
        val navController: NavController =
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
        navController.navigate(R.id.action_measurement)
    }
}
