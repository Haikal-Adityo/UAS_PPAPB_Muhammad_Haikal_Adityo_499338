package com.example.nutricare_uas

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.nutricare_uas.databinding.ActivityAddDetailBinding
import com.example.nutricare_uas.model.AdminFood
import com.example.nutricare_uas.recyclerViewAdapter.UserFoodRecyclerViewAdapter
import com.example.nutricare_uas.userFood.UserFood
import com.example.nutricare_uas.userFood.UserFoodDao
import com.example.nutricare_uas.userFood.UserFoodRoomDatabase
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddDetailActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddDetailBinding.inflate(layoutInflater)
    }

    private lateinit var mediator: TabLayoutMediator
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            viewPager2 = viewPager
            viewPager.adapter = DetailFoodTabAdapter(supportFragmentManager, this@AddDetailActivity.lifecycle)
            mediator = TabLayoutMediator(tabLayout, viewPager)
            {tab, position ->
                when(position){
                    0-> tab.text = "Discover"
                    1-> tab.text = "Make Your Own"
                }
            }
            mediator.attach()
        }

        binding.btnBack.setOnClickListener{
            finish()
        }

    }

}