package vn.edu.usth.uihealthcare.Data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import vn.edu.usth.uihealthcare.Data.BaseEntity

@Entity(tableName = "weight_table")
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) override val id: Long = 0,
    val weight: Float,
    val timestamp: Long
) : BaseEntity()