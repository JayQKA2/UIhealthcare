package vn.edu.usth.uihealthcare

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.sensor.StepsSensorService
import vn.edu.usth.uihealthcare.utils.HealthConnectManager

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationBar: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var permissionLauncher: ActivityResultLauncher<Set<String>>

    private val hiddenBottomNavDestinations = setOf(
        R.id.navigation_heart, R.id.navigation_sleep, R.id.stepsActivity,
        R.id.navigation_measurement, R.id.aboutAppFragment2, R.id.helpFragment
    )

    @SuppressLint("MissingPermission", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestPermissions()

        permissionLauncher = registerForActivityResult(
            healthConnectManager.requestPermissionsActivityContract()
        ) { grantedPermissions ->
            if (grantedPermissions.containsAll(healthConnectManager.permission)) {
                Log.d("HEALTH_CONNECT", "Permissions granted!")
            } else {
                Log.d("HEALTH_CONNECT", "Permissions denied!")
            }
        }

        val healthConnectClient = healthConnectManager.getHealthConnectClient(this)
        if (healthConnectClient == null) {
            Log.e("HealthConnect", "Health Connect is not available or needs an update.")
            return
        }

        setupUI()
        setupNavigation()
        startStepSensorService()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (!granted) {
                    Toast.makeText(this, "Notification permission is required!", Toast.LENGTH_SHORT).show()
                }
            }
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val requiredPermissions = healthConnectManager.permission
        lifecycleScope.launch {
            val hasPermissions = healthConnectManager.hasAllPermissions(requiredPermissions)
            if (!hasPermissions) {
                permissionLauncher.launch(requiredPermissions)
            }
        }
    }

    private val healthConnectManager by lazy { HealthConnectManager(this) }

    private fun setupUI() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        bottomNavigationBar = findViewById(R.id.nav_view)
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(bottomNavigationBar, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in hiddenBottomNavDestinations) {
                bottomNavigationBar.visibility = View.GONE
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                toolbar.visibility = View.VISIBLE
            } else {
                bottomNavigationBar.visibility = View.VISIBLE
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                toolbar.visibility = View.GONE
            }
        }
    }

    private fun startStepSensorService() {
        val serviceIntent = Intent(this, StepsSensorService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        const val MESSAGE_CAMERA_NOT_AVAILABLE = 3
    }
}
