package com.example.nutricare_uas

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.nutricare_uas.databinding.ActivityUpdateFireFoodBinding
import com.example.nutricare_uas.model.User
import com.example.nutricare_uas.savedFood.SavedFood
import com.example.nutricare_uas.savedFood.SavedFoodDao
import com.example.nutricare_uas.savedFood.SavedFoodRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpdateFireFoodActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityUpdateFireFoodBinding.inflate(layoutInflater)
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var savedFoodDao: SavedFoodDao
    private lateinit var executorService: ExecutorService
    private var updateId = ""
    private var time = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()
        val savedFoodDb = SavedFoodRoomDatabase.getDatabase(this)
        savedFoodDao = savedFoodDb!!.savedFoodDao()!!

        val foodId = intent.getStringExtra("food_id")
        if (foodId != null) {
            getFoodDetails(foodId)
        }

        val timeSpinner = binding.spinnerTime
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.food_time,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        timeSpinner.setAdapter(adapter)
        timeSpinner.setOnItemClickListener { parent, _, position, _ ->
            time = parent.getItemAtPosition(position).toString()
        }

        timeSpinner.setOnDismissListener {
        }

        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }

            btnAdd.setOnClickListener {
                if (!isSpinnerItemSelected()) {
                    spinnerTime.error = "Please select a time"
                    Toast.makeText(this@UpdateFireFoodActivity, "Please select a time", Toast.LENGTH_SHORT).show()
                } else {
                    val name = textinputFoodName.text.toString().trim()
                    val calorie = textinputCalorie.text.toString().toInt()
                    val serving = textinputServing.text.toString().toInt()

                    val savedFood = SavedFood(
                        time = time,
                        foodName = name,
                        calorie = calorie,
                        serving = serving,
                        userId = FirebaseAuth.getInstance().currentUser?.uid
                    )

                    insert(savedFood)
                    finish()

                    Toast.makeText(this@UpdateFireFoodActivity, "Food Saved Successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFoodDetails(foodId: String) {
        firestore.collection("foods").document(foodId).get()
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

    private fun isSpinnerItemSelected(): Boolean {
        return !TextUtils.isEmpty(time)
    }

    private fun insert(savedFood: SavedFood) {
        executorService.execute {
            savedFoodDao.insert(savedFood)
        }
    }
}
