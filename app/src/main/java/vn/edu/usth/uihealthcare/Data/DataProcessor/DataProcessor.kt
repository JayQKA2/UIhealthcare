package vn.edu.usth.uihealthcare.Data.DataProcessor


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.edu.usth.uihealthcare.Data.Entity.WeightEntity
import vn.edu.usth.uihealthcare.Data.ViewModel.WeightViewModel
import java.time.Instant
import java.time.ZoneOffset

object DataProcessor {

    suspend fun processWeightData(
        context: Context,
        weight: Double,
        healthConnectClient: HealthConnectClient?,
        weightViewModel: WeightViewModel
    ) {
        try {
            // Save weight to Health Connect
            val now = Instant.now()
            val weightRecord = WeightRecord(
                weight = Mass.kilograms(weight),
                time = now,
                zoneOffset = ZoneOffset.UTC
            )
            healthConnectClient?.insertRecords(listOf(weightRecord))
            Log.d("DataProcessor", "WeightRecord inserted: $weightRecord")

            // Save weight to Room database
            val weightEntity = WeightEntity(weight = weight.toFloat(), timestamp = now.toEpochMilli())
            withContext(Dispatchers.IO) {
                weightViewModel.insertWeight(weightEntity)
            }
            Log.d("DataProcessor", "WeightEntity inserted: $weightEntity")

            Toast.makeText(context, "Weight saved: $weight kg", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("DataProcessor", "Failed to save weight: ${e.message}")
            Toast.makeText(context, "Lưu thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


}