package com.example.nutricare_uas

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutricare_uas.databinding.FragmentDetailBinding
import com.example.nutricare_uas.model.User
import com.example.nutricare_uas.receiver.MidnightReceiver
import com.example.nutricare_uas.recyclerViewAdapter.SavedFoodRecyclerViewAdapter
import com.example.nutricare_uas.savedFood.SavedFood
import com.example.nutricare_uas.savedFood.SavedFoodDao
import com.example.nutricare_uas.savedFood.SavedFoodRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var savedFoodList: List<SavedFood>
    private lateinit var savedFoodDao: SavedFoodDao
    private lateinit var executorService: ExecutorService
    private lateinit var recyclerViewAdapter: SavedFoodRecyclerViewAdapter
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        executorService = Executors.newSingleThreadExecutor()
        val db = SavedFoodRoomDatabase.getDatabase(requireContext())
        savedFoodDao = db!!.savedFoodDao()!!

        savedFoodList = emptyList()

        recyclerViewAdapter = SavedFoodRecyclerViewAdapter(
            savedFoodList = savedFoodList,
            onClickSavedFood = { context, clickedSavedFood ->
                val intent = Intent(context, UpdateSavedFoodActivity::class.java).apply {
                    putExtra("food_id", clickedSavedFood.id)
                }
                context.startActivity(intent)
            }
        )

        with(binding) {
            rvFood.layoutManager = LinearLayoutManager(requireContext())
            rvFood.adapter = recyclerViewAdapter

            imgBtnClear.setOnClickListener {
                clearAllData()
            }

            btnClear.setOnClickListener {
                clearAllData()
            }

            btnAdd.setOnClickListener {
                val intent = Intent(requireContext(), AddDetailActivity::class.java)
                startActivity(intent)
            }

            ItemTouchHelper(simpleCallback).attachToRecyclerView(rvFood)
        }

        setMidnightAlarm()

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun observeSavedFoods(userId: String) {
        savedFoodDao.getAllSavedFoods(userId).observe(viewLifecycleOwner) { savedFoods ->
            if (savedFoods.isNotEmpty()) {
                savedFoodList = savedFoods
                recyclerViewAdapter.updateData(savedFoods)

                val totalCalorie = savedFoodList.sumOf { it.totalCalorie }
                binding.txtTotalCalorie.text = "$totalCalorie kcal"
            }
        }
    }

    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val swipedSavedFood = savedFoodList[position]

            showDeleteConfirmationDialog(swipedSavedFood) { shouldDelete ->
                if (shouldDelete) {
                    delete(swipedSavedFood)

                    Log.d(
                        "SavedList",
                        "Deleted item at position $position: ${swipedSavedFood.foodName} (Time: ${swipedSavedFood.time})"
                    )

                    savedFoodList = savedFoodList.toMutableList().also { it.removeAt(position) }

                    Log.d("SavedList", "Updated list: $savedFoodList")

                    recyclerViewAdapter.updateData(savedFoodList)
                } else {
                    recyclerViewAdapter.notifyItemChanged(position)
                }
            }
        }

    }

    private fun showDeleteConfirmationDialog(food: SavedFood, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Delete Food")
            .setMessage("Are you sure you want to delete ${food.foodName}?")
            .setPositiveButton("Confirm") { _, _ ->
                callback(true)
            }
            .setNegativeButton("Cancel") { _, _ ->
                callback(false)
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showClearAllConfirmationDialog(callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Clear All Data")
            .setMessage("Are you sure you want to clear all data?")
            .setPositiveButton("Confirm") { _, _ ->
                callback(true)
            }
            .setNegativeButton("Cancel") { _, _ ->
                callback(false)
            }

        val dialog = builder.create()
        dialog.show()
    }

    fun clearAllData() {
        showClearAllConfirmationDialog { confirmed ->
            if (confirmed) {
                executorService.execute {
                    if (userId != null) {
                        savedFoodDao.clearAllSavedFoods(userId)

                        viewLifecycleOwner.lifecycleScope.launch {
                            savedFoodList = emptyList()
                            recyclerViewAdapter.updateData(savedFoodList)

                            updateTotalCalorieDisplay()
                            updateUserTargetCalorieFromFirestore()
                        }
                    }
                }
            }
        }
    }

    private fun delete(savedFood: SavedFood) {
        executorService.execute {
            savedFoodDao.delete(savedFood)

            updateTotalCalorieDisplay()

            if (savedFoodList.isEmpty()) {
                updateUserTargetCalorieFromFirestore()
            }
        }
    }

    private fun updateUserTargetCalorieFromFirestore() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userID = user.uid
            val userDocumentRef = firestore.collection("users").document(userID)

            userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                val userProfile = documentSnapshot?.toObject(User::class.java)
                val targetCalorieFromFirestore = userProfile?.targetCalorie ?: 0

                if (userProfile != null) {
                    userDocumentRef.update("currentCalorie", targetCalorieFromFirestore)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "currentCalorie updated successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(ContentValues.TAG, "Error updating currentCalorie", exception)
                        }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalCalorieDisplay() {
        val totalCalorie = savedFoodList.sumOf { it.totalCalorie }
        binding.txtTotalCalorie.text = "$totalCalorie kcal"
    }

    @SuppressLint("ShortAlarm")
    fun setMidnightAlarm() {
        val midnightIntent = Intent(requireContext(), MidnightReceiver::class.java)
        midnightIntent.action = "CLEAR_ALL_DATA_ALARM"

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            midnightIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }


    override fun onResume() {
        super.onResume()
        userId?.let {
            observeSavedFoods(it)
        }
    }

}
