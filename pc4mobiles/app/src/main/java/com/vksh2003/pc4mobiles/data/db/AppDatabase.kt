package com.vksh2003.pc4mobiles.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vksh2003.pc4mobiles.data.dao.AirportDao
import com.vksh2003.pc4mobiles.data.dao.FavoriteDao
import com.vksh2003.pc4mobiles.data.entity.Airport
import com.vksh2003.pc4mobiles.data.entity.Favorite

@Database(
    entities = [Airport::class, Favorite::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flight_search.db"
                )
                    .createFromAsset("database/flight_search.db")
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}