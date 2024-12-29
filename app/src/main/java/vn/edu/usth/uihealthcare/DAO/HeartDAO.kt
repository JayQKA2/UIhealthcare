package vn.edu.usth.uihealthcare.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vn.edu.usth.uihealthcare.entity.Heart

@Dao
interface HeartDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRate(heartRate: Heart.HeartRate)

    @Query("SELECT * FROM heartrate WHERE user_id = :userId")
    suspend fun getHeartRateByUserId(userId: Int): List<Heart.HeartRate>
}