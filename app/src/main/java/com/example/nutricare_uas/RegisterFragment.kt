package com.example.nutricare_uas

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.nutricare_uas.databinding.FragmentRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentRegisterBinding? = null
    private val binding get()= _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    private var firestore = Firebase.firestore
    private val userCollectionRef = firestore.collection("users")

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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        val authActivity = requireActivity() as? AuthActivity

        firestore = FirebaseFirestore.getInstance()

        with(binding) {

            btnRegister.setOnClickListener {
                val username = binding.textinputName.text.toString().trim()
                val email = binding.textinputEmail.text.toString().trim()
                val password = binding.textinputPass.text.toString()

                if (username.isEmpty()) {
                    binding.textinputName.error = "Username required"
                } else if (username.length > 30) {
                    binding.textinputName.error = "Username should not exceed 30 characters"
                } else if (email.isEmpty()) {
                    binding.textinputEmail.error = "E-mail required"
                } else if (password.isEmpty()) {
                    binding.textinputPass.error = "Password required"
                } else {
                    userCollectionRef.whereEqualTo("email", email).get()
                        .addOnCompleteListener { emailCheckTask ->
                            if (emailCheckTask.isSuccessful) {
                                val documents = emailCheckTask.result?.documents
                                if (documents.isNullOrEmpty()) {
                                    val intent = Intent(requireContext(), UserDataActivity::class.java)
                                    intent.putExtra("username", username)
                                    intent.putExtra("email", email)
                                    intent.putExtra("password", password)
                                    startActivity(intent)

                                    binding.textinputName.text?.clear()
                                    binding.textinputEmail.text?.clear()
                                    binding.textinputPass.text?.clear()
                                } else {
                                    Toast.makeText(this@RegisterFragment.requireContext(), "This email is already in use", Toast.LENGTH_SHORT).show()
                                    binding.textinputEmail.error = "This email is already in use"
                                }
                            } else {
                                binding.textinputEmail.error = "Email uniqueness check failed: ${emailCheckTask.exception?.message}"
                            }
                        }
                }
            }

            txtToLogin.setOnClickListener{
                authActivity?.switchFragment(0)
                binding.textinputName.text?.clear()
                binding.textinputEmail.text?.clear()
                binding.textinputPass.text?.clear()
            }

        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}