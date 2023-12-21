package com.example.nutricare_uas.savedFood

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nutricare_uas.userFood.UserFood

@Dao
interface SavedFoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(savedFood: SavedFood)

    @Update
    fun update(savedFood: SavedFood)

    @Delete
    fun delete(savedFood: SavedFood)

    @Query("SELECT * FROM saved_food_table WHERE id = :foodId")
    fun getFoodById(foodId: Int): SavedFood?

    @Query("SELECT * FROM saved_food_table WHERE userId = :userId ORDER BY CASE WHEN time = 'Breakfast' THEN 1 WHEN time = 'Lunch' THEN 2 WHEN time = 'Dinner' THEN 3 ELSE 0 END, id ASC")
    fun getAllSavedFoods(userId: String): LiveData<List<SavedFood>>

    @Query("SELECT SUM(totalCalorie) FROM saved_food_table WHERE userId = :userId")
    fun getTotalCalorieByUserId(userId: String): Int

    @Query("DELETE FROM saved_food_table WHERE userId = :userId")
    fun clearAllSavedFoods(userId: String)

}