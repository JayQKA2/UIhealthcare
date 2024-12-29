package vn.edu.usth.uihealthcare.Data

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Update

abstract class BaseDao<T : BaseEntity?> {
    @Insert(onConflict = REPLACE)
    abstract fun insert(entity: T)

    @Update
    abstract fun update(entity: T)

    @Delete
    abstract fun delete(entity: T)
}