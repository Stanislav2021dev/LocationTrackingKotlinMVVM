package com.example.locationtask8.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "coordinates_table")
data class CoordinatesModel(
    @ColumnInfo(name = "coordinates_id")
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    @ColumnInfo(name = "coordinates_date_time")
    var dateTime:String,

    @ColumnInfo(name="coordinetes_coordinates")
    var coordinates:String) {

    fun getIdDb(): Long = id
    fun getDateTimeDb():String=dateTime
    fun getCoordinatesDb():String=coordinates

}