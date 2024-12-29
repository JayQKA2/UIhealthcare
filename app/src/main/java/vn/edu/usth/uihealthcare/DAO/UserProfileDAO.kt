package vn.edu.usth.uihealthcare.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vn.edu.usth.uihealthcare.entity.UserProfile


    @Dao
    interface UserProfileDAO {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertUserProfile(userProfile: UserProfile)

        @Query("SELECT * FROM userprofile WHERE user_id = :userId")
        suspend fun getUserProfileById(userId: Int): UserProfile?
    }
