package com.example.nutricare_uas.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AdminFood(
    @set:Exclude @get:Exclude @Exclude var id: String = "",
    var foodName: String? = "",
    var calorie: Int? = null,
    var serving: Int? = 100,
    var totalCalorie: Int? = null,

    @ServerTimestamp
    var timestamp: Date? = null

) {
    init {
        totalCalorie = calculateTotalCalorie()
    }

    private fun calculateTotalCalorie(): Int {
        val result = ((serving ?: 100) / 100f * (calorie ?: 0)).toInt()
        return if (result < 0) 0 else result
    }

}


