package com.example.nutricare_uas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.nutricare_uas.databinding.ActivityAdminUpdateBinding
import com.example.nutricare_uas.model.AdminFood
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AdminUpdateActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAdminUpdateBinding.inflate(layoutInflater)
    }

    private val firestore = Firebase.firestore
    private var updateId = ""
    private var foodCollectionRef = firestore.collection("foods")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val foodId = intent.getStringExtra("food_id")
        if (foodId != null) {
            getFoodDetails(foodId)
        }


        with(binding) {

            btnBack.setOnClickListener {
                finish()
            }

            btnSave.setOnClickListener {
                val name = binding.textinputFoodName.text.toString()
                val calorie = binding.textinputCalorie.text.toString().toInt()
                val serving = binding.textinputServing.text.toString().toInt()

                val newFood = AdminFood(
                    id = updateId,
                    foodName = name,
                    calorie = calorie,
                    serving = serving
                )

                updateFood(newFood)
                finish()
            }

        }

    }

    private fun getFoodDetails(foodId: String) {
        foodCollectionRef.document(foodId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val foodName = documentSnapshot.getString("foodName")
                    val calorie = documentSnapshot.getLong("calorie")
                    val serving = documentSnapshot.getLong("serving")

                    runOnUiThread {
                        updateId = foodId
                        binding.textinputFoodName.setText(foodName)
                        binding.textinputCalorie.setText(calorie?.toString() ?: "")
                        binding.textinputServing.setText(serving?.toString() ?: "")
                    }
                } else {
                    Log.d("AdminCrudFragment", "Document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("AdminCrudFragment", "Error getting food details: ", exception)
            }
    }

    private fun updateFood(food: AdminFood) {
        foodCollectionRef.document(food.id).set(food)
            .addOnSuccessListener {
                Log.d("AdminCrudFragment", "Food updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.d("AdminCrudFragment", "Error updating food: ", exception)
            }
    }

}