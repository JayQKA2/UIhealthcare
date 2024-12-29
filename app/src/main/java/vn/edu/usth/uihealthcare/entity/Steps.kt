package vn.edu.usth.uihealthcare.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
        tableName = "steps",
        foreignKeys = [
            ForeignKey(
                entity = UserProfile::class,
                parentColumns = ["user_id"],
                childColumns = ["user_id"],
                onDelete = ForeignKey.CASCADE
            )
        ]
    )
    data class Steps(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "steps_id") val stepsId: Int = 0,
        @ColumnInfo(name = "user_id") val userId: Int?,
        @ColumnInfo(name = "day_steps") val daySteps: String?,
        @ColumnInfo(name = "steps") val steps: Int?
    )
