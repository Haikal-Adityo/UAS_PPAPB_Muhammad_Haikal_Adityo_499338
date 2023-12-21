package com.example.nutricare_uas

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.nutricare_uas.databinding.FragmentHomeBinding
import com.example.nutricare_uas.model.User
import com.example.nutricare_uas.receiver.Notification
import com.example.nutricare_uas.receiver.messageExtra
import com.example.nutricare_uas.receiver.titleExtra
import com.example.nutricare_uas.savedFood.SavedFood
import com.example.nutricare_uas.savedFood.SavedFoodDao
import com.example.nutricare_uas.savedFood.SavedFoodRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var savedFoodDao: SavedFoodDao
    private lateinit var executorService: ExecutorService

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationManager: NotificationManager

    private var title: String = ""
    private var message: String = ""
    private var currentCalorie: Int = 0
    private var totalCalorie: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        executorService = Executors.newSingleThreadExecutor()
        val db = SavedFoodRoomDatabase.getDatabase(requireContext())
        savedFoodDao = db!!.savedFoodDao()!!

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        observeUserProfile()
        userId?.let {
            observeSavedFoods(it)
        }

        return view
    }


    private fun observeUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userID = user.uid
            val userDocumentRef = firestore.collection("users").document(userID)

            userDocumentRef.addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e(ContentValues.TAG, "Error listening for user profile changes", error)
                    return@addSnapshotListener
                }

                val userProfile = documentSnapshot?.toObject(User::class.java)
                updateUI(userProfile)

                if ((currentCalorie) > 0) {
                    scheduleNotificationAt(8)
                    scheduleNotificationAt(13)
                    scheduleNotificationAt(18)
                }
            }
        }
    }

    private fun observeSavedFoods(userId: String) {
        savedFoodDao.getAllSavedFoods(userId).observe(viewLifecycleOwner) { savedFoods ->
            totalCalorie = 0
            var breakfastCalorie = 0
            var lunchCalorie = 0
            var dinnerCalorie = 0

            for (savedFood in savedFoods) {
                totalCalorie += savedFood.totalCalorie
                when (savedFood.time) {
                    "Breakfast" -> breakfastCalorie += savedFood.totalCalorie
                    "Lunch" -> lunchCalorie += savedFood.totalCalorie
                    "Dinner" -> dinnerCalorie += savedFood.totalCalorie
                }
            }

            val currentUser = auth.currentUser
            currentUser?.let { user ->
                val userID = user.uid
                val userDocumentRef = firestore.collection("users").document(userID)

                userDocumentRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        val userProfile = documentSnapshot?.toObject(User::class.java)

                        if (userProfile != null) {
                            val targetCalorie = userProfile.targetCalorie ?: 0
                            currentCalorie = (targetCalorie - totalCalorie).coerceAtLeast(0)

                            binding.txtTotalCalorie.text = "$totalCalorie"
                            binding.txtBreakfast.text = "$breakfastCalorie"
                            binding.txtLunch.text = "$lunchCalorie"
                            binding.txtDinner.text = "$dinnerCalorie"

                            updateUI(userProfile)
                        }
                    }
                    .addOnFailureListener { error ->
                        Log.e(ContentValues.TAG, "Error retrieving user profile", error)
                    }
            }
        }
    }

    private fun updateUI(userProfile: User?) {
        userProfile?.let {

            binding.txtName.text = it.username
            binding.txtCurrentCalorie.text = currentCalorie.toString()
            binding.txtTargetCalorie.text = it.targetCalorie.toString()

            val maxCalorie = it.targetCalorie ?: 0
            val progress = (currentCalorie.toDouble() / maxCalorie * 100).toInt()
            updateProgressBar(progress)
        }
    }

    private fun updateProgressBar(progress: Int) {
        val progressBar = binding.progressBar
        progressBar.progress = progress
    }

    private fun scheduleNotificationAt(hourOfDay: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (System.currentTimeMillis() < calendar.timeInMillis) {
            val currentUser = auth.currentUser
            currentUser?.let { user ->
                val userID = user.uid
                val userDocumentRef = firestore.collection("users").document(userID)

                userDocumentRef.get()
                    .addOnSuccessListener { _ ->
                        val remainingCalories = currentCalorie

                        if (remainingCalories > 0) {
                            title = when (hourOfDay) {
                                8 -> "Start of the day being healthy!"
                                13 -> "Don't give up!"
                                18 -> "Just a little bit more!"
                                else -> "Don't give up!"
                            }
                            message = "You still have $remainingCalories calorie${if (remainingCalories > 1) "s" else ""} left"
                        } else {
                            title = "Keep up the great work!"
                            message = "You have cleared the target calorie for the day"
                        }

                        val intent = Intent(this@HomeFragment.requireContext(), Notification::class.java)
                        intent.putExtra(titleExtra, title)
                        intent.putExtra(messageExtra, message)
                        val pendingIntent = PendingIntent.getBroadcast(
                            requireContext(),
                            hourOfDay,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    }
                    .addOnFailureListener { error ->
                        Log.e(ContentValues.TAG, "Error retrieving user profile", error)
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        observeUserProfile()
        userId?.let {
            observeSavedFoods(it)
        }
    }

}