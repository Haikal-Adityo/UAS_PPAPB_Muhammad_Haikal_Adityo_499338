package com.example.nutricare_uas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.nutricare_uas.databinding.ActivityAuthBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAuthBinding.inflate(layoutInflater)
    }
    private lateinit var mediator: TabLayoutMediator
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            viewPager2 = viewPager
            viewPager.adapter = AuthTabAdapter(supportFragmentManager, this@AuthActivity.lifecycle)
            mediator = TabLayoutMediator(tabLayout, viewPager)
            {tab, position ->
                when(position){
                    0-> tab.text = "Login"
                    1-> tab.text = "Register"
                }
            }
            mediator.attach()
        }

    }

    fun switchFragment(position: Int){
        viewPager2.currentItem = position
    }

    fun makeToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}