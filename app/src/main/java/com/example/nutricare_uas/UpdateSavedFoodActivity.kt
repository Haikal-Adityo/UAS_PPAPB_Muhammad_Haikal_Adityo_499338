package com.example.nutricare_uas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.nutricare_uas.databinding.ActivityUpdateSavedFoodBinding
import com.example.nutricare_uas.savedFood.SavedFood
import com.example.nutricare_uas.savedFood.SavedFoodDao
import com.example.nutricare_uas.savedFood.SavedFoodRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpdateSavedFoodActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityUpdateSavedFoodBinding.inflate(layoutInflater)
    }

    private lateinit var savedFoodDao: SavedFoodDao
    private lateinit var executorService: ExecutorService
    private var time = ""
    private lateinit var adapter: ArrayAdapter<CharSequence>

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        executorService = Executors.newSingleThreadExecutor()

        val savedFoodDb = SavedFoodRoomDatabase.getDatabase(this)
        savedFoodDao = savedFoodDb!!.savedFoodDao()!!

        val foodId = intent.getIntExtra("food_id", 0)
        getFoodDetails(foodId)

        val timeSpinner = binding.spinnerTime
        adapter = ArrayAdapter.createFromResource(
            this,
            R.array.food_time,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        timeSpinner.setAdapter(adapter)
        timeSpinner.setOnItemClickListener { _, _, position, _ ->
            time = adapter.getItem(position).toString()
        }

        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }

            btnSave.setOnClickListener {
                val time = spinnerTime.text.toString()
                val name = textinputFoodName.text.toString().trim()
                val calorie = textinputCalorie.text.toString().toInt()
                val serving = textinputServing.text.toString().toInt()

                val newFood = SavedFood(
                    id = foodId,
                    time = time,
                    foodName = name,
                    calorie = calorie,
                    serving = serving,
                    userId = FirebaseAuth.getInstance().currentUser?.uid
                )

                update(newFood)
                finish()
            }
        }
    }

    private fun getFoodDetails(foodId: Int) {
        executorService.execute {
            val existingFood = savedFoodDao.getFoodById(foodId)

            runOnUiThread {
                existingFood?.let {
                    binding.spinnerTime.setText(it.time, false)

                    binding.textinputFoodName.setText(it.foodName)
                    binding.textinputCalorie.setText(it.calorie?.toString() ?: "")
                    binding.textinputServing.setText(it.serving?.toString() ?: "")
                }
            }
        }
    }


    private fun update(food: SavedFood) {
        executorService.execute {
            savedFoodDao.update(food)
        }
    }
}
