package com.example.nutricare_uas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nutricare_uas.databinding.ActivityUserDataBinding
import com.example.nutricare_uas.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class UserDataActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityUserDataBinding.inflate(layoutInflater)
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var userID: String? = null
    private lateinit var userDocumentRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        userID = currentUser?.uid

        if (userID != null) {
            userDocumentRef = firestore.collection("users").document(userID!!)
        }

        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")
        val isAdmin = false

        binding.btnConfirm.setOnClickListener {
            val weight = binding.textinputWeight.text.toString().toInt()
            val height = binding.textinputHeight.text.toString().toInt()
            val calorie = binding.textinputCalorie.text.toString().toInt()

            val userData = User(
                username = username,
                email = email,
                isAdmin = isAdmin,
                weight = weight,
                height = height,
                targetCalorie = calorie,
            )

            firebaseAuth.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener { accountCreationTask ->
                    if (accountCreationTask.isSuccessful) {
                        val userID = accountCreationTask.result?.user?.uid

                        if (userID != null) {
                            firestore.collection("users").document(userID)
                                .set(userData)
                                .addOnSuccessListener {
                                    makeToast("Account successfully created")
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    makeToast("Error saving user data: ${e.message}")
                                }
                        } else {
                            makeToast("User ID is null after account creation")
                        }
                    } else {
                        makeToast("Account creation failed: ${accountCreationTask.exception?.message}")
                    }
                }
        }

    }

    private fun makeToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

