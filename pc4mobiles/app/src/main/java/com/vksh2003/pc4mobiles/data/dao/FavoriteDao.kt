package com.vksh2003.pc4mobiles.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vksh2003.pc4mobiles.data.entity.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("""
        SELECT * FROM favorite
        WHERE departure_code = :departureCode
        AND destination_code = :destinationCode
        LIMIT 1
    """)
    suspend fun getFavoriteByCodes(
        departureCode: String,
        destinationCode: String
    ): Favorite?

    @Query("SELECT * FROM favorite ORDER BY id ASC")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: Favorite)

    @Delete
    suspend fun delete(favorite: Favorite)
}