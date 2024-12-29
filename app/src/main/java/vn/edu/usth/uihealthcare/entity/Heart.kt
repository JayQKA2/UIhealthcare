package vn.edu.usth.uihealthcare.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

class Heart {
    @Entity(
        tableName = "heartrate",
        foreignKeys = [
            ForeignKey(
                entity = UserProfile::class,
                parentColumns = ["user_id"],
                childColumns = ["user_id"],
                onDelete = ForeignKey.CASCADE
            )
        ]
    )
    data class HeartRate(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "heart_id") val heartId: Int = 0,
        @ColumnInfo(name = "user_id") val userId: Int?,
        @ColumnInfo(name = "recorded") val recorded: String?,
        @ColumnInfo(name = "heart_rate") val heartRate: Int?,
        @ColumnInfo(name = "blood_pressure") val bloodPressure: Int?
    )
}