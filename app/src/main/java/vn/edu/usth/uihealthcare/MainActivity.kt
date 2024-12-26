package vn.edu.usth.uihealthcare

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
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
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationBar: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val TAG = "BluetoothConnection"
    private val deviceName = "MySmartWatch"
    private var bluetoothSocket: BluetoothSocket? = null


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
        lifecycleScope.launch {
            setupHealthConnect()
        }

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth không được hỗ trợ trên thiết bị này", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth đang bị tắt. Vui lòng bật Bluetooth.", Toast.LENGTH_SHORT).show()
            } else {
                connectToSmartWatch()
            }
        }
    }

    private val healthConnectManager by lazy {
        HealthConnectManager(this)
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


    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.BLUETOOTH_SCAN"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add("android.permission.BLUETOOTH_SCAN")
        }

        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.BLUETOOTH_CONNECT"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add("android.permission.BLUETOOTH_CONNECT")
        }


        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.ACCESS_FINE_LOCATION"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add("android.permission.ACCESS_FINE_LOCATION")
        }

        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.INTERNET"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
            data =
                android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
        }
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun connectToSmartWatch() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        if (pairedDevices.isNullOrEmpty()) {
            Toast.makeText(this, "Không có thiết bị Bluetooth nào được ghép nối", Toast.LENGTH_SHORT).show()
            return
        }

        val device = pairedDevices.find { it.name == deviceName }

        if (device != null) {
            try {
                connectToDevice(device)
            } catch (e: IOException) {
                Log.e(TAG, "Lỗi khi kết nối với thiết bị", e)
                Toast.makeText(this, "Kết nối thất bại", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Không tìm thấy thiết bị: $deviceName", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun connectToDevice(device: BluetoothDevice) {
        val uuid = device.uuids[0].uuid
        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

        bluetoothAdapter?.cancelDiscovery()

        bluetoothSocket?.let {
            it.connect()
            Toast.makeText(this, "Kết nối thành công với ${device.name}", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Kết nối thành công với ${device.name}")
        } ?: run {
            Log.e(TAG, "Không thể tạo socket kết nối")
            Toast.makeText(this, "Không thể kết nối với thiết bị", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothSocket?.close()
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
