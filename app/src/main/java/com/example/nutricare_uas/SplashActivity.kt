package com.example.nutricare_uas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


private const val PREFS_NAME = "MyPrefsFile"
private const val PREF_EMAIL = "email"
private const val PREF_REMEMBER_ME = "remember_me"

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT:Long = 2000

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            val sharedPreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val rememberMe = sharedPreferences.getBoolean(PREF_REMEMBER_ME, false)

            if (rememberMe) {
                val email = sharedPreferences.getString(PREF_EMAIL, "")
                if (firebaseAuth.currentUser != null) {
                    if (email != null) {
                        checkUserRole(email)
                    }
                }
            } else {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }
        }, SPLASH_TIME_OUT)
    }

    private fun checkUserRole(email: String) {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val isAdmin = userDocument.getBoolean("admin") ?: false

                    if (isAdmin) {
                        val intent = Intent(this, AdminActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener { e ->
                makeToast("Error retrieving user data: $e")
            }
    }

    private fun makeToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}