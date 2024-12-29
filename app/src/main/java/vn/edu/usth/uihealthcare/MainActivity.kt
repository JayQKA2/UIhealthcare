package vn.edu.usth.uihealthcare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.utils.HealthConnectManager

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationBar: BottomNavigationView
    private lateinit var toolbar: Toolbar

    private val hiddenBottomNavDestinations = setOf(
        R.id.navigation_test,
        R.id.navigation_sleep,
        R.id.navigation_step,
        R.id.navigation_heart,
        R.id.navigation_measurement,
        R.id.navigation_sleep2
    )

    private val healthConnectManager by lazy {
        HealthConnectManager(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            val status = healthConnectManager.checkForHealthConnectInstalled(this@MainActivity)
            when (status) {
                HealthConnectClient.SDK_UNAVAILABLE -> {
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    redirectToHealthConnectPlayStore()
                }
                HealthConnectClient.SDK_AVAILABLE -> {
                    if (!healthConnectManager.checkPermissions()) {
                        requestPermissions()
                    }
                }
            }

            setupUI()
            setupNavigation()
        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(healthConnectManager.PERMISSIONS.toTypedArray())
    }

    private fun redirectToHealthConnectPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
        }
        startActivity(intent)
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
}