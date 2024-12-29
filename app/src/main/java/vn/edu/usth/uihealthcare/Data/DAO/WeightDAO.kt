package vn.edu.usth.uihealthcare.Data.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import vn.edu.usth.uihealthcare.Data.BaseDao
import vn.edu.usth.uihealthcare.Data.Entity.WeightEntity

@Dao
abstract class WeightDao : BaseDao<WeightEntity>() {
    @Query("SELECT * FROM weight_table ORDER BY timestamp DESC")
    abstract fun getAllWeights(): LiveData<List<WeightEntity>>
}