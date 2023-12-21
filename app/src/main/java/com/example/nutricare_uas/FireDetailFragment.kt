package com.example.nutricare_uas

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutricare_uas.databinding.FragmentAdminListBinding
import com.example.nutricare_uas.databinding.FragmentFireDetailBinding
import com.example.nutricare_uas.model.AdminFood
import com.example.nutricare_uas.recyclerViewAdapter.AdminRecyclerViewAdapter
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FireDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FireDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFireDetailBinding? = null
    private val binding get()= _binding!!

    private lateinit var recyclerViewAdapter: AdminRecyclerViewAdapter
    private lateinit var foodList: List<AdminFood>

    private val firestore = Firebase.firestore
    private val foodListLiveData : MutableLiveData<List<AdminFood>>
            by lazy {
                MutableLiveData<List<AdminFood>>()
            }
    private var foodCollectionRef = firestore.collection("foods")

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
        _binding = FragmentFireDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerViewAdapter = AdminRecyclerViewAdapter(
            foodList = emptyList(),
            onClickFood = { context, clickedFood ->
                val intent = Intent(context, UpdateFireFoodActivity::class.java).apply {
                    putExtra("food_id", clickedFood.id)
                }
                context.startActivity(intent)
            }
        )

        with(binding) {
            rvFood.layoutManager = LinearLayoutManager(requireContext())
            rvFood.adapter = recyclerViewAdapter

            editSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    performSearch(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            btnSearch.setOnClickListener {
                performSearch(editSearch.text.toString())
            }
        }

        observeFoods()
        getAllFoods()

        return view
    }

    private fun performSearch(query: String) {
        val filteredList = foodList.filter { food ->
            food.foodName?.contains(query, true) ?: false
        }

        recyclerViewAdapter.updateData(filteredList)
    }


    private fun getAllFoods() {
        foodCollectionRef.addSnapshotListener {
                snapshots,
                error ->
            if (error != null) {
                Log.d("MainActivity", "Error listening for note changes ", error)
                return@addSnapshotListener
            }

            val foods = arrayListOf<AdminFood>()
            snapshots?.forEach {
                    documentReference ->
                foods.add((
                        AdminFood(
                            documentReference.id,
                            documentReference.getString("foodName") ?: "",
                            documentReference.getLong("calorie")?.toInt() ?: 0,
                            documentReference.getLong("serving")?.toInt() ?: 0,
                            documentReference.getLong("totalCalorie")?.toInt() ?: 0,
                        )
                        ))
            }

            if (foods != null) {
                foodListLiveData.postValue(foods)
            }

        }
    }

    private fun observeFoods() {
        foodListLiveData.observe(viewLifecycleOwner) {
                foods ->
            if (foods.isNotEmpty()) {
                for (note in foods) {
                    Log.d("RoomLog", "getAllFoods: ${note.foodName}")
                }
                foodList = foods
                recyclerViewAdapter.updateData(foods)
            } else {
                Log.d("RoomLog", "getAllFoods: empty")
            }
        }
    }

}