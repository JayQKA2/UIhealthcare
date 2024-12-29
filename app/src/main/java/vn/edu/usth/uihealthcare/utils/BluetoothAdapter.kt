package vn.edu.usth.uihealthcare.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

class BluetoothHelper(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothScanReceiver: BroadcastReceiver? = null

    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }

    @SuppressLint("MissingPermission")
    fun startBluetoothScan() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }

        bluetoothScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action: String? = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Log.d(
                        "BluetoothScan",
                        "Device found: ${device?.name ?: "Unknown"} - ${device?.address}"
                    )
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                    Log.d("BluetoothScan", "Bluetooth scan finished")
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(bluetoothScanReceiver, filter)
        bluetoothAdapter?.startDiscovery()
        Log.d("BluetoothScan", "Bluetooth scan started")
    }

    @SuppressLint("MissingPermission")
    fun stopBluetoothScan() {
        bluetoothAdapter?.cancelDiscovery()
        bluetoothScanReceiver?.let {
            context.unregisterReceiver(it)
            Log.d("BluetoothScan", "Bluetooth receiver unregistered")
        }
        bluetoothScanReceiver = null
    }
}
