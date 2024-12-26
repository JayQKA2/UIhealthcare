package vn.edu.usth.uihealthcare.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class BluetoothHelper(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

//    fun getConnectedDevices(): List<BluetoothDevice>? {
//        return if (ActivityCompat.checkSelfPermission(context, "android.permission.BLUETOOTH_CONNECT") == PackageManager.PERMISSION_GRANTED) {
//            bluetoothAdapter?.bondedDevices?.filter {
//                it.bondState == BluetoothDevice.BOND_BONDED
//            }
//        } else {
//            null  // Trả về null nếu không có quyền
//        }
//    }
//    fun isDeviceConnected(deviceAddress: String): Boolean {
//        return getConnectedDevices()?.any { it.address == deviceAddress } == true
//    }
    fun getConnectedDevices(): List<BluetoothDevice>? {
        val connectedDevices = mutableListOf<BluetoothDevice>()
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        // Kiểm tra quyền
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            val bluetoothProfile = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
            connectedDevices.addAll(bluetoothProfile)
        } else {
            // Trả về null hoặc xử lý theo cách khác nếu không có quyền
            return null
        }

        return connectedDevices
    }
}