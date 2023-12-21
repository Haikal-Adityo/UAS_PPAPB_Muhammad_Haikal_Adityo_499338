package com.example.nutricare_uas.userFood

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserFood::class],
    version = 1,
    exportSchema = false
)
abstract class UserFoodRoomDatabase : RoomDatabase() {
    abstract fun userFoodDao(): UserFoodDao

    companion object {
        @Volatile
        private var INSTANCE: UserFoodRoomDatabase? = null

        fun getDatabase(context: Context): UserFoodRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserFoodRoomDatabase::class.java,
                    "user_food_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
