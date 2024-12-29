package vn.edu.usth.uihealthcare.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vn.edu.usth.uihealthcare.DAO.HeartDAO
import vn.edu.usth.uihealthcare.DAO.SleepDAO
import vn.edu.usth.uihealthcare.DAO.StepsDAO
import vn.edu.usth.uihealthcare.DAO.UserProfileDAO

import vn.edu.usth.uihealthcare.entity.Heart
import vn.edu.usth.uihealthcare.entity.Sleep
import vn.edu.usth.uihealthcare.entity.Steps
import vn.edu.usth.uihealthcare.entity.UserProfile


    @Database(
        entities = [UserProfile::class, Sleep::class, Heart::class, Steps::class],
        version = 1,
        exportSchema = false
    )
    abstract class AppDatabase : RoomDatabase() {
        abstract fun userProfileDao(): UserProfileDAO
        abstract fun sleepDao(): SleepDAO
        abstract fun heartRateDao(): HeartDAO
        abstract fun stepsDao(): StepsDAO

        companion object {
            @Volatile
            private var INSTANCE: AppDatabase? = null

            fun getDatabase(context: Context): AppDatabase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    ).build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
