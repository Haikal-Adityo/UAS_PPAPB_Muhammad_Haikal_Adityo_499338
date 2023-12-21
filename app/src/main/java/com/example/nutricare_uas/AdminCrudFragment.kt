package com.example.nutricare_uas

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.nutricare_uas.databinding.FragmentAdminCrudBinding
import com.example.nutricare_uas.model.AdminFood
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class AdminCrudFragment : Fragment() {
    private var _binding: FragmentAdminCrudBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()

    private var foodCollectionRef = firestore.collection("foods")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCrudBinding.inflate(inflater, container, false)
        val view = binding.root

        with(binding) {
            btnAdd.setOnClickListener {
                val foodName = textInputLayoutFoodName.editText?.text.toString()
                val calorieStr = textinputCalorie.text.toString()

                if (foodName.isEmpty()) {
                    binding.textInputLayoutFoodName.error = "Food name required"
                } else if (calorieStr.isEmpty()) {
                    binding.textinputCalorie.error = "Calorie required"
                } else {
                    val calorie = calorieStr.toInt()

                    val newFood = AdminFood(
                        foodName = foodName,
                        calorie = calorie
                    )

                    addFood(newFood)

                    textInputLayoutFoodName.editText?.text?.clear()
                    textinputCalorie.text?.clear()

                    val action = AdminCrudFragmentDirections.actionAdminCrudFragmentToAdminListFragment()
                    findNavController().navigate(action)
                }
            }
        }

        return view
    }

    private fun addFood(food: AdminFood) {
        foodCollectionRef.add(food).addOnFailureListener {
            Log.d("AdminCrudFragment", "Error adding food: ", it)
        }
    }

}
