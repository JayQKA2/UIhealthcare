package vn.edu.usth.uihealthcare.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
        tableName = "sleep",
        foreignKeys = [
            ForeignKey(
                entity = UserProfile::class,
                parentColumns = ["user_id"],
                childColumns = ["user_id"],
                onDelete = ForeignKey.CASCADE
            )
        ]
    )
    data class Sleep(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "sleep_id") val sleepId: Int = 0,
        @ColumnInfo(name = "user_id") val userId: Int?,
        @ColumnInfo(name = "sleep_started") val sleepStarted: String?,
        @ColumnInfo(name = "sleep_ended") val sleepEnded: String?,
        @ColumnInfo(name = "duration") val duration: Float?
    )
