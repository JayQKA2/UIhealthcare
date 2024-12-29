package vn.edu.usth.uihealthcare.Data.AppDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import vn.edu.usth.uihealthcare.Data.DAO.WeightDao
import vn.edu.usth.uihealthcare.Data.Entity.WeightEntity

@Database(entities = [WeightEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao
}