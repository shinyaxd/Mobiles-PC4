package com.vksh2003.pc4mobiles.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "iata_code")
    val iataCode: String,
    @ColumnInfo(name = "passengers")
    val passengers: Int
)