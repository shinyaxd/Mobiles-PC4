package com.vksh2003.pc4mobiles.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {
    @Query("SELECT * FROM airport WHERE name LIKE '%' || :searchQuery || '%' OR iata_code LIKE '%' || :searchQuery || '%'")
    fun searchAirports(searchQuery: String): Flow<List<Airport>>
    @Query("SELECT * FROM airport ORDER BY passengers DESC")
    fun getAllAirports(): Flow<List<Airport>>
}