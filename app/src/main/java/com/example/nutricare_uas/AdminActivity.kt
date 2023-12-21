package com.example.nutricare_uas

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.nutricare_uas.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth

private const val PREFS_NAME = "MyPrefsFile"
private const val PREF_EMAIL = "email"
private const val PREF_PASSWORD = "password"
private const val PREF_IS_ADMIN = "isAdmin"
private const val PREF_REMEMBER_ME = "remember_me"

class AdminActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAdminBinding.inflate(layoutInflater)
    }

    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        with(binding) {
            val navController = findNavController(R.id.nav_host_fragment)
            bottomNavigationView.setupWithNavController(navController)
        }

    }

}