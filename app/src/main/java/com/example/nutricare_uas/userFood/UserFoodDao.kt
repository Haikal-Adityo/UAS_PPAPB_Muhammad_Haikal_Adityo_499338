package com.example.nutricare_uas.userFood

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserFoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userFood: UserFood)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(userFood: UserFood)

    @Update
    fun update(userFood: UserFood)

    @Delete
    fun delete(userFood: UserFood)

    @Query("SELECT * FROM food_table WHERE id = :foodId")
    fun getFoodById(foodId: Int): UserFood?

    @Query("DELETE FROM food_table WHERE userId = :userId")
    fun deleteAllUserFoods(userId: String)

    @Query("SELECT * FROM food_table WHERE userId = :userId ORDER BY id DESC")
    fun getAllUserFoods(userId: String): LiveData<List<UserFood>>

}

