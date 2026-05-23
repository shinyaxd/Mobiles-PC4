package com.vksh2003.pc4mobiles.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.vksh2003.pc4mobiles.data.entity.Airport
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query("""
        SELECT * FROM airport
        WHERE name LIKE '%' || :searchQuery || '%'
           OR iata_code LIKE '%' || :searchQuery || '%'
        ORDER BY passengers DESC
    """)
    fun searchAirports(searchQuery: String): Flow<List<Airport>>

    @Query("""
        SELECT * FROM airport
        WHERE iata_code != :departureCode
        ORDER BY passengers DESC
    """)
    fun getDestinationAirports(departureCode: String): Flow<List<Airport>>

    @Query("""
        SELECT * FROM airport
        WHERE iata_code = :iataCode
        LIMIT 1
    """)
    fun getAirportByIataCode(iataCode: String): Flow<Airport?>

    @Query("SELECT * FROM airport ORDER BY name ASC")
    fun getAllAirports(): Flow<List<Airport>>
}