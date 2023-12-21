package com.example.nutricare_uas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.nutricare_uas.databinding.ActivityEditProfileBinding
import com.example.nutricare_uas.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityEditProfileBinding.inflate(layoutInflater)
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var userID: String? = null
    private lateinit var userDocumentRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        userID = currentUser?.uid

        if (userID != null && userID!!.isNotEmpty()) {
            userDocumentRef = firestore.collection("users").document(userID!!)
            getUserDetails()
        } else {
            Toast.makeText(this, "User ID is null or empty", Toast.LENGTH_SHORT).show()
        }

        getUserDetails()

        binding.btnBack.setOnClickListener{
            finish()
        }

        binding.btnSave.setOnClickListener {
            userDocumentRef.get()
                .addOnSuccessListener { _ ->
                    val username = binding.textinputName.text.toString().trim()
                    val heightStr = binding.textinputHeight.text.toString()
                    val weightStr = binding.textinputWeight.text.toString()
                    val newTargetCalorieStr = binding.textinputTargetCalorie.text.toString()

                    val height = heightStr.toIntOrNull()
                    val weight = weightStr.toIntOrNull()
                    val newTargetCalorie = newTargetCalorieStr.toIntOrNull()

                    if (username.length in 1..30 &&
                        height != null && weight != null && newTargetCalorie != null &&
                        height >= 0 && weight >= 0 && newTargetCalorie >= 0
                    ) {
                        val email = currentUser?.email

                        val updatedUser = User(
                            id = userID ?: "",
                            username = username,
                            email = email,
                            height = height,
                            weight = weight,
                            targetCalorie = newTargetCalorie,
                        )

                        showConfirmationDialog(updatedUser)

                    } else {
                        binding.textinputName.error = "Username should be between 1 and 30 characters."
                        when {
                            height == null || height < 0 -> binding.textinputHeight.error = "Invalid height. Please enter a valid number."
                            weight == null || weight < 0 -> binding.textinputWeight.error = "Invalid weight. Please enter a valid number."
                            newTargetCalorie == null || newTargetCalorie < 0 -> binding.textinputTargetCalorie.error = "Invalid target calorie. Please enter a valid number."
                            else -> Toast.makeText(this, "Invalid input. Please enter valid numbers.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("EditProfileActivity", "Error getting user details from Firestore", exception)
                }
        }

    }

    private fun getUserDetails() {
        userDocumentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val username = documentSnapshot.getString("username")
                    val height = documentSnapshot.getLong("height")
                    val weight = documentSnapshot.getLong("weight")
                    val targetCalorie = documentSnapshot.getLong("targetCalorie")

                    runOnUiThread {
                        binding.textinputName.setText(username)
                        binding.textinputHeight.setText(height?.toString() ?: "")
                        binding.textinputWeight.setText(weight?.toString() ?: "")
                        binding.textinputTargetCalorie.setText(targetCalorie?.toString() ?: "")
                    }
                } else {
                    Log.d("EditProfileActivity", "Document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("EditProfileActivity", "Error getting user details: ", exception)
            }
    }

    private fun updateUser(user: User) {
        userDocumentRef.set(user)
            .addOnSuccessListener {
                Log.d("EditProfileActivity", "User updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.d("EditProfileActivity", "Error updating user: ", exception)
            }
    }

    private fun showConfirmationDialog(user: User) {
        val confirmationDialog = AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to save these changes?")
            .setPositiveButton("Save") { _, _ ->
                updateUser(user)
                finish()
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()

        confirmationDialog.show()
    }

}