package com.example.nutricare_uas.userFood

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_table")
data class UserFood(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    val id: Int = 0,

    @ColumnInfo(name = "foodName")
    val foodName: String?,

    @ColumnInfo(name = "calorie")
    val calorie: Int?,

    @ColumnInfo(name = "serving")
    val serving: Int? = 100,

    @ColumnInfo(name = "totalCalorie")
    var totalCalorie: Int = 0,

    @ColumnInfo(name = "userId")
    var userId: String?,

) {
    init {
        totalCalorie = calculateTotalCalorie()
    }

    private fun calculateTotalCalorie(): Int {
        val result = ((serving ?: 100) / 100f * (calorie ?: 0)).toInt()
        return if (result < 0) 0 else result
    }

}
