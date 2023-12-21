package com.example.nutricare_uas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.nutricare_uas.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

private const val PREFS_NAME = "MyPrefsFile"
private const val PREF_EMAIL = "email"
private const val PREF_PASSWORD = "password"
private const val PREF_REMEMBER_ME = "remember_me"

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userID: String? = null
    private lateinit var userDocumentRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        userID = firebaseAuth.currentUser?.uid

        if (userID != null) {
            userDocumentRef = firestore.collection("users").document(userID!!)
            retrieveUserData()
        }

        with(binding) {
            val navController = findNavController(R.id.nav_host_fragment)
            bottomNavigationView.setupWithNavController(navController)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun retrieveUserData() {
        userDocumentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val targetCalorieLong = documentSnapshot.getLong("targetCalorie")
                    val currentCalorieLong = documentSnapshot.getLong("currentCalorie")
                    val targetCalorie = targetCalorieLong?.toInt() ?: 0
                    val currentCalorie = currentCalorieLong?.toInt() ?: 0

//                    binding.txtTargetCalorie.text = "Target calorie: $targetCalorie"
//                    binding.txtCurrentCalorie.text = "Current calorie: $currentCalorie"
                } else {
//                    binding.txtTargetCalorie.text = "Calorie information not available"
//                    binding.txtCurrentCalorie.text = "Calorie information not available"
                }
            }
            .addOnFailureListener { e ->
//                binding.txtTargetCalorie.text = "Failed to retrieve calorie information"
//                binding.txtCurrentCalorie.text = "Failed to retrieve calorie information"
                Log.e("MainActivity", "Error retrieving user data: $e")
            }
    }

    private fun clearLoginCredentials() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.remove(PREF_EMAIL)
        editor.remove(PREF_PASSWORD)
        editor.remove(PREF_REMEMBER_ME)

        editor.apply()
    }

    private fun makeToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
