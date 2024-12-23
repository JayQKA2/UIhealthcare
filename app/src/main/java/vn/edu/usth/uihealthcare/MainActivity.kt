package vn.edu.usth.uihealthcare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import vn.edu.usth.uihealthcare.utils.HealthConnectUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {



    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                fetchHealthData()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val bottomNavigationBar: BottomNavigationView = findViewById(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        NavigationUI.setupWithNavController(bottomNavigationBar, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_test ||
                destination.id == R.id.navigation_sleep ||
                destination.id == R.id.navigation_step ||
                destination.id == R.id.navigation_heart ||
                destination.id == R.id.navigation_measurement
            ) {
                bottomNavigationBar.visibility = View.GONE
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                toolbar.visibility = View.VISIBLE
            } else {
                bottomNavigationBar.visibility = View.VISIBLE
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                toolbar.visibility = View.GONE
            }
        }

        lifecycleScope.launch {
            val status = HealthConnectUtils.checkForHealthConnectInstalled(this@MainActivity)

            when (status) {
                HealthConnectClient.SDK_UNAVAILABLE -> {
                    Toast.makeText(this@MainActivity, "Health Connect is unavailable on this device.", Toast.LENGTH_LONG).show()
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    Toast.makeText(
                        this@MainActivity,
                        "Health Connect needs to be installed or updated.",
                        Toast.LENGTH_LONG
                    ).show()
                    redirectToHealthConnectPlayStore()
                }
                HealthConnectClient.SDK_AVAILABLE -> {
                    if (!HealthConnectUtils.checkPermissions()) {
                        requestPermissions()
                    } else {
                        fetchHealthData()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun redirectToHealthConnectPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
        }
        startActivity(intent)
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(HealthConnectUtils.PERMISSIONS.toTypedArray())
    }

    private fun fetchHealthData() {
        lifecycleScope.launch {
            try {
                val stepsData = HealthConnectUtils.readStepsForInterval(7)
                Log.d("HealthData", "Steps Data: $stepsData")

                val distanceData = HealthConnectUtils.readDistanceForInterval(7)
                Log.d("HealthData", "Distance Data: $distanceData")

                val minutesData = HealthConnectUtils.readMinsForInterval(7)
                Log.d("HealthData", "Minutes Data: $minutesData")

                val sleepData = HealthConnectUtils.readSleepSessionsForInterval(7)
                Log.d("HealthData", "Sleep Data: $sleepData")

                Toast.makeText(this@MainActivity, "Health data fetched successfully.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("HealthDataError", "Error fetching health data", e)
                Toast.makeText(this@MainActivity, "Error fetching health data.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
