package vn.edu.usth.uihealthcare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.sensor.CameraService
import vn.edu.usth.uihealthcare.sensor.StepsSensorService
import vn.edu.usth.uihealthcare.utils.HealthConnectManager


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationBar: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var cameraService: CameraService
    private lateinit var mainViewModel: MainViewModel
    private lateinit var permissionLauncher: ActivityResultLauncher<Set<String>>

    private val hiddenBottomNavDestinations = setOf(
        R.id.navigation_heart,
        R.id.navigation_test,
        R.id.navigation_sleep,
        R.id.navigation_step,
        R.id.navigation_heart,
        R.id.navigation_measurement,
        R.id.navigation_sleep2,
    )

    @SuppressLint("MissingPermission", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        permissionLauncher = registerForActivityResult(
            healthConnectManager.requestPermissionsActivityContract()
        ) { grantedPermissions ->
            if (grantedPermissions.containsAll(healthConnectManager.permission)) {
                Log.d("HEALTH_CONNECT", "Permissions granted!")
            } else {
                Log.d("HEALTH_CONNECT", "Permissions denied!")
            }
        }

        checkAndRequestHealthConnectPermissions()
        setupUI()
        setupNavigation()

        val serviceIntent = Intent(this, StepsSensorService::class.java)
        startService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, StepsSensorService::class.java))
        cameraService.stop()
    }

    private fun checkAndRequestHealthConnectPermissions() {
        val requiredPermissions = healthConnectManager.permission
        lifecycleScope.launch {
            val hasPermissions = healthConnectManager.hasAllPermissions(requiredPermissions)
            if (!hasPermissions) {
                permissionLauncher.launch(requiredPermissions)
            }
        }
    }

    private val healthConnectManager by lazy {
        HealthConnectManager(this)
    }

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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        const val MESSAGE_CAMERA_NOT_AVAILABLE = 3
    }
}
