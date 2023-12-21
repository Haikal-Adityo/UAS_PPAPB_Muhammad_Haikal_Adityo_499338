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
import com.example.nutricare_uas.databinding.ActivityUpdateRoomFoodBinding
import com.example.nutricare_uas.model.User
import com.example.nutricare_uas.savedFood.SavedFood
import com.example.nutricare_uas.savedFood.SavedFoodDao
import com.example.nutricare_uas.savedFood.SavedFoodRoomDatabase
import com.example.nutricare_uas.userFood.UserFood
import com.example.nutricare_uas.userFood.UserFoodDao
import com.example.nutricare_uas.userFood.UserFoodRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpdateRoomFoodActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityUpdateRoomFoodBinding.inflate(layoutInflater)
    }

    private lateinit var userFoodDao: UserFoodDao
    private lateinit var savedFoodDao: SavedFoodDao
    private lateinit var executorService: ExecutorService
    private var time = ""

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        executorService = Executors.newSingleThreadExecutor()

        val userFoodDb = UserFoodRoomDatabase.getDatabase(this)
        userFoodDao = userFoodDb!!.userFoodDao()!!

        val savedFoodDb = SavedFoodRoomDatabase.getDatabase(this)
        savedFoodDao = savedFoodDb!!.savedFoodDao()!!

        val foodId = intent.getIntExtra("food_id", 0)
        getFoodDetails(foodId)

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

            btnSave.setOnClickListener {
                val name = textinputFoodName.text.toString().trim()
                val calorie = textinputCalorie.text.toString().toInt()
                val serving = textinputServing.text.toString().toInt()

                val newFood = UserFood(
                    id = foodId,
                    foodName = name,
                    calorie = calorie,
                    serving = serving,
                    userId = FirebaseAuth.getInstance().currentUser?.uid
                )

                update(newFood)
                finish()
            }

            btnAdd.setOnClickListener {
                if (!isSpinnerItemSelected()) {
                    Toast.makeText(this@UpdateRoomFoodActivity, "Please select a time", Toast.LENGTH_SHORT).show()
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

                    Toast.makeText(this@UpdateRoomFoodActivity, "Food Saved Successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFoodDetails(foodId: Int) {
        executorService.execute {
            val existingFood = userFoodDao.getFoodById(foodId)

            runOnUiThread {
                existingFood?.let {
                    binding.textinputFoodName.setText(it.foodName)
                    binding.textinputCalorie.setText(it.calorie?.toString() ?: "")
                    binding.textinputServing.setText(it.serving?.toString() ?: "")
                }
            }
        }
    }

    private fun isSpinnerItemSelected(): Boolean {
        return !TextUtils.isEmpty(time)
    }

    private fun update(food: UserFood) {
        executorService.execute {
            userFoodDao.update(food)
        }
    }

    private fun insert(savedFood: SavedFood) {
        executorService.execute {
            savedFoodDao.insert(savedFood)
        }
    }
}