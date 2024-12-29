package vn.edu.usth.uihealthcare.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vn.edu.usth.uihealthcare.entity.Sleep

@Dao
interface SleepDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleep(sleep: Sleep)

    @Query("SELECT * FROM sleep WHERE user_id = :userId")
    suspend fun getSleepByUserId(userId: Int): List<Sleep>
}