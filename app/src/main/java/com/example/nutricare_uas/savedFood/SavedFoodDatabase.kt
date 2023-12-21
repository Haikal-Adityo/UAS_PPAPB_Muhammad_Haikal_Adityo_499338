package com.example.nutricare_uas.savedFood

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedFood::class],
    version = 1,
    exportSchema = false
)
abstract class SavedFoodRoomDatabase : RoomDatabase() {
    abstract fun savedFoodDao(): SavedFoodDao

    companion object {
        @Volatile
        private var INSTANCE: SavedFoodRoomDatabase? = null

        fun getDatabase(context: Context): SavedFoodRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavedFoodRoomDatabase::class.java,
                    "saved_food_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

