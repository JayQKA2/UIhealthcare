package vn.edu.usth.uihealthcare.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.feature.ExperimentalFeatureAvailabilityApi
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.readRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.io.IOException
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import vn.edu.usth.uihealthcare.model.DataRecord
import vn.edu.usth.uihealthcare.model.DataType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import java.util.concurrent.TimeUnit


const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    private var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)

    val permission = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)

    )

    init {
        checkAvailability()
    }

    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

    fun checkForHealthConnectInstalled(context: Context):Int {
        val availabilityStatus =
            HealthConnectClient.getSdkStatus(context, "com.google.android.apps.healthdata")
        when (availabilityStatus) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                }
                context.startActivity(intent)
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
            }
            SDK_AVAILABLE -> {
                val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.healthdata")
                if (intent != null) {
                    context.startActivity(intent)
                }
            }
        }
        return availabilityStatus
    }

    private fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    suspend fun checkPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted != null) {
            return granted.containsAll(permission)
        }
        return false
    }


    @OptIn(ExperimentalFeatureAvailabilityApi::class)
    fun isFeatureAvailable(feature: Int): Boolean{
        return healthConnectClient
            .features
            .getFeatureStatus(feature) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
    }

    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * TODO: Build [WeightRecord].
     */

    suspend fun writeWeightInput(weightInput: Double) {
        val time = ZonedDateTime.now().withNano(0)
        val weightRecord = WeightRecord(
            weight = Mass.kilograms(weightInput),
            time = time.toInstant(),
            zoneOffset = time.offset
        )
        val records = listOf(weightRecord)
        try {
            healthConnectClient.insertRecords(records)
            Toast.makeText(context, "Successfully insert records", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun readWeightInputs(start: Instant, end: Instant): List<WeightRecord> {
        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }


    suspend fun computeWeeklyAverage(start: Instant, end: Instant): Mass? {
        val request = AggregateRequest(
            metrics = setOf(WeightRecord.WEIGHT_AVG),
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.aggregate(request)
        return response[WeightRecord.WEIGHT_AVG]
    }


    private var count: Long = 0

    private fun incrementStepCount(): Long {
        count++
        return count
    }

    suspend fun writeStepsInput(start: ZonedDateTime, end: ZonedDateTime, count: Long) {
        if (count < 1) {
            Log.e("StepError", "Invalid count: $count. Count must be at least 1.")
            return
        }

        val stepsRecord = StepsRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            count = incrementStepCount()
        )
        val records = listOf(stepsRecord)
        healthConnectClient.insertRecords(records)
        Log.d("StepRecord", "Steps record inserted: $count steps")
    }


    suspend fun readStepsRecords(interval : Long): List<DataRecord> {
        val startTime: ZonedDateTime =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(interval-1)

        val endTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()).minusMinutes(1)
            .plusSeconds(59)
        val response =
            healthConnectClient?.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toLocalDate().atStartOfDay(),
                        endTime.toLocalDateTime()
                    ),
                    timeRangeSlicer = Period.ofDays(1)
                )
            )
        if (response != null) {
            val stepsData = mutableListOf<DataRecord>()
            response.sortedBy { it.startTime }
            var trackTime = startTime.toLocalDate().atStartOfDay()
            for (dailyResult in response) {
                if (dailyResult.startTime.isAfter(trackTime)) {
                    while (trackTime.isBefore(dailyResult.startTime)) {
                        stepsData.add(
                            DataRecord(
                                metricValue = "0",
                                dataType = DataType.STEPS,
                                toDatetime = trackTime.toLocalDate().atTime(LocalTime.MAX)
                                    .atZone(ZoneId.systemDefault()).format(
                                        dateTimeFormatter
                                    ),
                                fromDatetime = if (trackTime.toLocalDate()
                                        .isEqual(startTime.toLocalDate())
                                ) startTime.format(dateTimeFormatter) else trackTime.atZone(ZoneId.systemDefault())
                                    .format(dateTimeFormatter)
                            )
                        )
                        trackTime = trackTime.plusDays(1)
                    }
                }
                val totalSteps = dailyResult.result[StepsRecord.COUNT_TOTAL]
                stepsData.add(
                    DataRecord(
                        metricValue = (totalSteps ?: 0).toString(),
                        dataType = DataType.STEPS,
                        toDatetime = dailyResult.endTime.atZone(ZoneId.systemDefault())
                            .minusSeconds(1)
                            .format(dateTimeFormatter),
                        fromDatetime = if (dailyResult.startTime.toLocalDate()
                                .isEqual(startTime.toLocalDate())
                        ) startTime.format(
                            dateTimeFormatter
                        ) else dailyResult.startTime.atZone(ZoneId.systemDefault())
                            .format(dateTimeFormatter)
                    )
                )
                trackTime = dailyResult.endTime
            }

            while (trackTime.isBefore(endTime.toLocalDateTime()) && Duration.between(trackTime,endTime).toMinutes()>1) {
                stepsData.add(
                    DataRecord(
                        metricValue = "0",
                        dataType = DataType.STEPS,
                        toDatetime = if (trackTime.toLocalDate().isEqual(endTime.toLocalDate()))
                            endTime.format(dateTimeFormatter)
                        else trackTime.toLocalDate().atTime(LocalTime.MAX)
                            .atZone(ZoneId.systemDefault())
                            .format(dateTimeFormatter),
                        fromDatetime = if (trackTime.toLocalDate()
                                .isEqual(startTime.toLocalDate())
                        )
                            startTime.format(dateTimeFormatter)
                        else trackTime.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
                    )
                )
                trackTime = trackTime.plusDays(1).toLocalDate().atStartOfDay()
            }
            Log.d("Data", stepsData.toString())
            return stepsData
        }
        return emptyList()
    }

    suspend fun readCaloriesRecords(start: Instant, end: Instant): List<TotalCaloriesBurnedRecord> {
        val request = ReadRecordsRequest(
            recordType = TotalCaloriesBurnedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }


    /**
     * TODO: Build [HeartRateRecord].
     */

    private fun buildHeartRateSeries(
        sessionStartTime: ZonedDateTime,
        sessionEndTime: ZonedDateTime,
    ): HeartRateRecord {
        val samples = mutableListOf<HeartRateRecord.Sample>()
        var time = sessionStartTime
        while (time.isBefore(sessionEndTime)) {
            samples.add(
                HeartRateRecord.Sample(
                    time = time.toInstant(),
                    beatsPerMinute = (80 + Random.nextInt(80)).toLong()
                )
            )
            time = time.plusSeconds(30)
        }
        return HeartRateRecord(
            startTime = sessionStartTime.toInstant(),
            startZoneOffset = sessionStartTime.offset,
            endTime = sessionEndTime.toInstant(),
            endZoneOffset = sessionEndTime.offset,
            samples = samples
        )
    }

    /**
     * TODO: Build [SleepSessionRecord].
     */

    suspend fun writeSleepSession(healthConnectClient: HealthConnectClient, start: ZonedDateTime, end: ZonedDateTime) {
        val sleepSessionRecord = SleepSessionRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset
        )
        healthConnectClient.insertRecords(listOf(sleepSessionRecord))
    }

    suspend fun readSleepSession(healthConnectClient: HealthConnectClient, start: Instant, end: Instant): List<SleepSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        val sleepRecords = response.records
        for (sleepRecord in sleepRecords) {
            println("Sleep record: ${sleepRecord.startTime} to ${sleepRecord.endTime}")
        }
        return sleepRecords
    }


    suspend fun getChangesToken(): String {
        return healthConnectClient.getChangesToken(
            ChangesTokenRequest(
                setOf(
                    ExerciseSessionRecord::class,
                    StepsRecord::class,
                    TotalCaloriesBurnedRecord::class,
                    HeartRateRecord::class,
                    WeightRecord::class
                )
            )
        )
    }


    suspend fun getChanges(token: String): Flow<ChangesMessage> = flow {
        var nextChangesToken = token
        do {
            val response = healthConnectClient.getChanges(nextChangesToken)
            if (response.changesTokenExpired) {

                throw IOException("Changes token has expired")
            }
            emit(ChangesMessage.ChangeList(response.changes))
            nextChangesToken = response.nextChangesToken
        } while (response.hasMore)
        emit(ChangesMessage.NoMoreChanges(nextChangesToken))
    }

    private suspend inline fun <reified T : Record> readData(
        timeRangeFilter: TimeRangeFilter,
        dataOriginFilter: Set<DataOrigin> = setOf(),
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }

    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }

}

enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}
