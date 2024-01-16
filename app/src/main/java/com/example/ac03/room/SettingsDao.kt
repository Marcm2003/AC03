package com.example.ac03.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {

    @Query("SELECT iva FROM settings WHERE id = 1")
    fun getIvaPercentage(): Float?

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Settings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSettings(settings: Settings)
}
