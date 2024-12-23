package vn.edu.usth.uihealthcare.utils

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import kotlinx.coroutines.CoroutineScope
import vn.edu.usth.uihealthcare.utils.HealthConnectUtils
import kotlinx.coroutines.launch

@Composable
fun HealthConnectManager() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val interval: Long = 7

    var steps by remember { mutableStateOf("0") }
    var mins by remember { mutableStateOf("0") }
    var distance by remember { mutableStateOf("0") }
    var sleepDuration by remember { mutableStateOf("00:00") }

    var showHealthConnectInstallPopup by remember { mutableStateOf(false) }

    val requestPermissionsLauncher =
        rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) { grantedPermissions ->
            if (grantedPermissions.containsAll(HealthConnectUtils.PERMISSIONS)) {
                fetchHealthData(scope, interval, onError = {
                    Toast.makeText(context, "Error fetching data: $it", Toast.LENGTH_SHORT).show()
                }) { stepsData, minsData, distanceData, sleepData ->
                    steps = stepsData
                    mins = minsData
                    distance = distanceData
                    sleepDuration = sleepData
                }
            } else {
                Toast.makeText(context, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

    LaunchedEffect(key1 = true) {
        when (HealthConnectUtils.checkForHealthConnectInstalled(context)) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Toast.makeText(
                    context,
                    "Health Connect client is not available for this device",
                    Toast.LENGTH_SHORT
                ).show()
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                showHealthConnectInstallPopup = true
            }
            HealthConnectClient.SDK_AVAILABLE -> {
                if (HealthConnectUtils.checkPermissions()) {
                    fetchHealthData(scope, interval, onError = {
                        Toast.makeText(context, "Error fetching data: $it", Toast.LENGTH_SHORT).show()
                    }) { stepsData, minsData, distanceData, sleepData ->
                        steps = stepsData
                        mins = minsData
                        distance = distanceData
                        sleepDuration = sleepData
                    }
                } else {
                    requestPermissionsLauncher.launch(HealthConnectUtils.PERMISSIONS)
                }
            }
        }
    }

    if (showHealthConnectInstallPopup) {
        val uriString = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
            }
        )
    }
}

private fun fetchHealthData(
    scope: CoroutineScope,
    interval: Long,
    onError: (String) -> Unit,
    onSuccess: (steps: String, mins: String, distance: String, sleepDuration: String) -> Unit
) {
    scope.launch {
        try {
            val steps = HealthConnectUtils.readStepsForInterval(interval).last().metricValue
            val mins = HealthConnectUtils.readMinsForInterval(interval).last().metricValue
            val distance = HealthConnectUtils.readDistanceForInterval(interval).last().metricValue
            val sleepDuration = HealthConnectUtils.readSleepSessionsForInterval(interval).last().metricValue
            onSuccess(steps, mins, distance, sleepDuration)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }
}

