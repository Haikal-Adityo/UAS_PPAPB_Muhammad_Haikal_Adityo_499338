package com.example.nutricare_uas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.nutricare_uas.databinding.ActivityAddFoodBinding
import com.example.nutricare_uas.userFood.UserFood
import com.example.nutricare_uas.userFood.UserFoodDao
import com.example.nutricare_uas.userFood.UserFoodRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddFoodActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddFoodBinding.inflate(layoutInflater)
    }

    private lateinit var mUserFoodDao: UserFoodDao
    private lateinit var executorService: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()
        val db = UserFoodRoomDatabase.getDatabase(this)
        mUserFoodDao = db!!.userFoodDao()!!

        with(binding) {

            btnBack.setOnClickListener {
                finish()
            }

            btnCreate.setOnClickListener {
                val foodName = textinputFoodName.text.toString().trim()
                val calorieText = textinputCalorie.text.toString()

                if (foodName.isNotBlank() && calorieText.isNotBlank()) {
                    val calorie = calorieText.toIntOrNull()

                    if (calorie != null) {
                        insert(UserFood(
                            foodName = foodName,
                            calorie = calorie,
                            serving = 100,
                            userId = FirebaseAuth.getInstance().currentUser?.uid
                        ))
                    } else {
                        showToast("Invalid calorie input")
                    }
                } else {
                    showToast("Please enter valid values")
                }

                finish()
            }
        }
    }

    private fun insert(userFood: UserFood) {
        executorService.execute {
            mUserFoodDao.insert(userFood)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
