package vn.edu.usth.uihealthcare.utils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
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
    val bluetoothAdapter = bluetoothManager.adapter

    // Kiểm tra Bluetooth có bật hay không
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        Log.d("BluetoothStatus", "Bluetooth is not enabled")
        return null // Bluetooth không được bật
    }

    // Kiểm tra quyền
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
        // Lấy danh sách thiết bị đang kết nối qua GATT
        val gattDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
        Log.d("BluetoothProfile", "GATT Connected devices: $gattDevices") // Log danh sách GATT

        // Nếu bạn cũng muốn kiểm tra các profile khác, hãy thêm vào đây
        val a2dpDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.A2DP)
        Log.d("BluetoothProfile", "A2DP Connected devices: $a2dpDevices") // Log danh sách A2DP

        // Thêm thiết bị vào danh sách kết nối
        connectedDevices.addAll(gattDevices)
        connectedDevices.addAll(a2dpDevices)
    } else {
        Log.d("BluetoothPermission", "Bluetooth permission is not granted")
        return null // Trả về null nếu không có quyền
    }

    return connectedDevices
}
}