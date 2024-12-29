package vn.edu.usth.uihealthcare.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


    @Entity(tableName = "userprofile")
    data class UserProfile(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "user_id") val userId: Int = 0,
        @ColumnInfo(name = "user_name") val userName: String,
        @ColumnInfo(name = "password") val password: String,
        @ColumnInfo(name = "dob") val dob: String?,
        @ColumnInfo(name = "weight") val weight: Float?,
        @ColumnInfo(name = "height") val height: Float?
    )
