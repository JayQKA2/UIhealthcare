package vn.edu.usth.uihealthcare.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vn.edu.usth.uihealthcare.entity.Steps

@Dao
interface StepsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: Steps)

    @Query("SELECT * FROM steps WHERE user_id = :userId")
    suspend fun getStepsByUserId(userId: Int): List<Steps>
}