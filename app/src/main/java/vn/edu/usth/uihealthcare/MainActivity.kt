package vn.edu.usth.uihealthcare

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.utils.BluetoothHelper
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

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestPermissions()

        setupUI()
        setupNavigation()
        setupBluetooth()
        enableBluetoothLauncher

        lifecycleScope.launch {
            setupHealthConnect()
        }

    }

    private val healthConnectManager by lazy {
        HealthConnectManager(this)
    }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth enabling cancelled", Toast.LENGTH_SHORT).show()
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


    private fun showMissingPermissions(permissions: List<String>) {
        val missingPermissions = permissions.joinToString(separator = "\n") { permission ->
            when (permission) {
                "android.permission.BLUETOOTH_SCAN" -> "Bluetooth Scan"
                "android.permission.BLUETOOTH_CONNECT" -> "Bluetooth Connect"
                "android.permission.ACCESS_FINE_LOCATION" -> "Access Fine Location"
                "android.permission.ACCESS_COARSE_LOCATION" -> "Access Coarse Location"
                "android.permission.INTERNET" -> "Internet Access"
                else -> permission
            }
        }
        Toast.makeText(
            this,
            "Missing permissions:\n$missingPermissions",
            Toast.LENGTH_LONG
        ).show()
    }

    private suspend fun setupHealthConnect() {
        val status = healthConnectManager.checkForHealthConnectInstalled(this)
        when (status) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Log.e("HealthConnect", "SDK unavailable")
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
    }

    private fun setupBluetooth() {
        val bluetoothHelper = BluetoothHelper(this)

        if (bluetoothHelper.isBluetoothSupported()) {
            if (bluetoothHelper.bluetoothAdapter?.isEnabled == true) {
                bluetoothHelper.startBluetoothScan()
            } else {
                Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_SCAN") != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add("android.permission.BLUETOOTH_SCAN")
        }

        if (ContextCompat.checkSelfPermission(this, "android.permission.BLUETOOTH_CONNECT") != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add("android.permission.BLUETOOTH_CONNECT")
        }


        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add("android.permission.ACCESS_FINE_LOCATION")
        }

        if (ContextCompat.checkSelfPermission(this, "android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add("android.permission.INTERNET")
        }

        if (permissionsToRequest.isNotEmpty()) {
            showMissingPermissions(permissionsToRequest)
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }


    private fun requestPermissions() {
        requestPermissionLauncher.launch(healthConnectManager.permission.toTypedArray())
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
