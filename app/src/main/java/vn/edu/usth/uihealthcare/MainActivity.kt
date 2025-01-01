package vn.edu.usth.uihealthcare

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import vn.edu.usth.uihealthcare.sensor.StepsSensorService
import vn.edu.usth.uihealthcare.utils.HealthConnectManager


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationBar: BottomNavigationView
    private lateinit var toolbar: Toolbar

    private val PHYSICAL_ACTIVITY_REQUEST_CODE = 100



    private val hiddenBottomNavDestinations = setOf(
        R.id.navigation_test,
        R.id.navigation_sleep,
        R.id.navigation_step,
        R.id.navigation_heart,
        R.id.navigation_measurement,
        R.id.navigation_sleep2
    )

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        checkAndRequestActivityRecognitionPermission()
        healthConnectManager
        setupUI()
        setupNavigation()
        val serviceIntent = Intent(this, StepsSensorService::class.java)
        startService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MM_StepsSensorService1", "onDestroy: ")
        stopService(Intent(this, StepsSensorService::class.java))
    }

    private fun checkAndRequestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGranted()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    PHYSICAL_ACTIVITY_REQUEST_CODE
                )
            }
        } else {
            onPermissionGranted()
        }
    }

    private fun onPermissionGranted() {
        Log.d("MM_StepsSensorService1", "onRequestPermissionsResult:Permission granted! ")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PHYSICAL_ACTIVITY_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                Log.d("MM_StepsSensorService1", "onRequestPermissionsResult:Permission denied! ")
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
}