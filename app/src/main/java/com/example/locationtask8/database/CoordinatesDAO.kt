package com.example.locationtask8.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface CoordinatesDAO {
    @Insert
    fun addCoordinates(coordinatesModel: CoordinatesModel?)

    @Query("DELETE FROM coordinates_table")
    fun deleteAllCoordinates()

    @Query("DELETE FROM coordinates_table WHERE coordinates_id=:id ")
    fun delete(id: Long)

    @Query("SELECT * FROM coordinates_table")
    fun getAllCoordinates(): List<CoordinatesModel?>?
}