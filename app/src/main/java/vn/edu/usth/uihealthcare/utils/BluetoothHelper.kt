package vn.edu.usth.uihealthcare.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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

    fun getConnectedDevices(): List<BluetoothDevice>? {
        return if (ActivityCompat.checkSelfPermission(context, "android.permission.BLUETOOTH_CONNECT") == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.bondedDevices?.filter {
                it.bondState == BluetoothDevice.BOND_BONDED
            }
        } else {
            null  // Trả về null nếu không có quyền
        }
    }
    fun isDeviceConnected(deviceAddress: String): Boolean {
        return getConnectedDevices()?.any { it.address == deviceAddress } == true
    }
}