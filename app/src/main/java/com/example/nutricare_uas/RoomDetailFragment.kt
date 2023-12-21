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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutricare_uas.databinding.FragmentAdminListBinding
import com.example.nutricare_uas.databinding.FragmentRoomDetailBinding
import com.example.nutricare_uas.recyclerViewAdapter.UserFoodRecyclerViewAdapter
import com.example.nutricare_uas.userFood.UserFood
import com.example.nutricare_uas.userFood.UserFoodDao
import com.example.nutricare_uas.userFood.UserFoodRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RoomDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoomDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentRoomDetailBinding? = null
    private val binding get()= _binding!!

    private lateinit var userFoodList: List<UserFood>
    private lateinit var userFoodDao: UserFoodDao
    private lateinit var executorService: ExecutorService
    private lateinit var recyclerViewAdapter: UserFoodRecyclerViewAdapter
    val userId = FirebaseAuth.getInstance().currentUser?.uid

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
        _binding = FragmentRoomDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        executorService = Executors.newSingleThreadExecutor()
        val db = UserFoodRoomDatabase.getDatabase(this@RoomDetailFragment.requireContext())
        userFoodDao = db!!.userFoodDao()!!

        recyclerViewAdapter = UserFoodRecyclerViewAdapter(
            userFoodList = emptyList(),
            onClickUserFood = { context, clickedUserFood ->
                val intent = Intent(context, UpdateRoomFoodActivity::class.java).apply {
                    putExtra("food_id", clickedUserFood.id)
                }
                context.startActivity(intent)
            }
        )

        with(binding) {
            rvFood.layoutManager = LinearLayoutManager(this@RoomDetailFragment.requireContext())
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

            btnCreate.setOnClickListener {
                val intent = Intent(this@RoomDetailFragment.requireContext(), AddFoodActivity::class.java)
                startActivity(intent)
            }

            ItemTouchHelper(simpleCallback).attachToRecyclerView(rvFood)
        }

        return view
    }

    private fun performSearch(query: String) {
        val filteredList = userFoodList.filter { userFood ->
            userFood.foodName?.contains(query, true) ?: false
        }
        recyclerViewAdapter.updateData(filteredList)
    }

    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val swipedUserFood = userFoodList[position]

            showDeleteConfirmationDialog(swipedUserFood) { shouldDelete ->
                if (shouldDelete) {
                    delete(swipedUserFood)

                    userFoodList = userFoodList.toMutableList().also { it.removeAt(position) }
                    recyclerViewAdapter.updateData(userFoodList)
                } else {
                    recyclerViewAdapter.notifyItemChanged(position)
                }
            }
        }
    }

    private fun observeUserFoods(userId: String) {
        Log.d("RoomLog", "observeUserFoods: Observing data for userId=$userId")
        userFoodDao.getAllUserFoods(userId).observe(this) { userFoods ->
            Log.d("RoomLog", "observeUserFoods: Size=${userFoods.size}")

            if (userFoods.isNotEmpty()) {
                for (userFood in userFoods) {
                    Log.d("RoomLog", "observeUserFoods: ${userFood.foodName}")
                }
                userFoodList = userFoods
                recyclerViewAdapter.updateData(userFoods)
            } else {
                Log.d("RoomLog", "observeUserFoods: empty")
            }
        }
    }

    private fun showDeleteConfirmationDialog(food: UserFood, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(this@RoomDetailFragment.requireContext())

        builder.setTitle("Delete Food")
            .setMessage("Are you sure you want to delete ${food.foodName}?")
            .setPositiveButton("Confirm") { _, _ ->
                callback(true)
            }
            .setNegativeButton("Cancel") { _, _ ->
                callback(false)
            }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()

        if (userId != null) {
            observeUserFoods(userId)
        }
    }

    private fun delete(userFood: UserFood) {
        executorService.execute {
            userFoodDao.delete(userFood)
        }
    }

}