package com.example.nutricare_uas.model

import com.google.firebase.firestore.Exclude

data class User(
    @set:Exclude @get:Exclude @Exclude var id: String = "",
    var username: String? = "",
    var email: String? = "",
    var isAdmin: Boolean? = false,
    var height: Int? = null,
    var weight: Int? = null,
    var targetCalorie: Int? = null,
)
