package com.example.nutricare_uas

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.nutricare_uas.databinding.FragmentProfileBinding
import com.example.nutricare_uas.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val PREFS_NAME = "MyPrefsFile"
private const val PREF_EMAIL = "email"
private const val PREF_PASSWORD = "password"
private const val PREF_REMEMBER_ME = "remember_me"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentProfileBinding? = null
    private val binding get()= _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        with(binding) {

            btnEditProfile.setOnClickListener {
                val intent = Intent(this@ProfileFragment.requireContext(), EditProfileActivity::class.java)
                startActivity(intent)
            }

            btnLogout.setOnClickListener {
                logoutUser()
            }
        }

        observeUserProfile()

        return view
    }

    private fun observeUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userID = user.uid
            val userDocumentRef = firestore.collection("users").document(userID)

            userDocumentRef.addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for user profile changes", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val userProfile = documentSnapshot.toObject(User::class.java)
                    updateUI(userProfile)
                } else {
                    Log.e(TAG, "User document not found for userID: $userID")
                }
            }
        }
    }

    private fun updateUI(userProfile: User?) {
        userProfile?.let {
            binding.txtName.text = it.username
            binding.txtHeight.text = it.height.toString()
            binding.txtWeight.text = it.weight.toString()
            binding.txtTargetCalorie.text = it.targetCalorie.toString()
        }
    }

    private fun clearLoginCredentials() {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.remove(PREF_EMAIL)
        editor.remove(PREF_PASSWORD)
        editor.remove(PREF_REMEMBER_ME)

        editor.apply()
    }

    private fun logoutUser() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout Confirmation")
        builder.setMessage("Are you sure you want to log out?")

        builder.setPositiveButton("Yes") { _, _ ->
            FirebaseAuth.getInstance().signOut()

            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

            clearLoginCredentials()

            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}