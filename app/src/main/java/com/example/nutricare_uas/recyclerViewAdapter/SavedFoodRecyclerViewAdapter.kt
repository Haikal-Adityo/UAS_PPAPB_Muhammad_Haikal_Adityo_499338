package com.example.nutricare_uas.recyclerViewAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.nutricare_uas.R
import com.example.nutricare_uas.databinding.ItemFoodBinding
import com.example.nutricare_uas.databinding.ItemSavedFoodBinding
import com.example.nutricare_uas.savedFood.SavedFood

class SavedFoodRecyclerViewAdapter(
    private var savedFoodList: List<SavedFood>,
    private val onClickSavedFood: (context: Context, SavedFood) -> Unit
) : RecyclerView.Adapter<SavedFoodRecyclerViewAdapter.ItemSavedFoodViewHolder>() {

    inner class ItemSavedFoodViewHolder(private val binding: ItemSavedFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(data: SavedFood) {
            with(binding) {
                txtFoodName.text = data.foodName
                txtTotalCalorie.text = data.totalCalorie.toString()

                when (data.time) {
                    "Breakfast" -> txtTime.setTextColor(itemView.context.getColor(R.color.breakfastColor))
                    "Lunch" -> txtTime.setTextColor(itemView.context.getColor(R.color.lunchColor))
                    "Dinner" -> txtTime.setTextColor(itemView.context.getColor(R.color.dinnerColor))
                }

                txtTime.text = data.time

                itemView.setOnClickListener {
                    onClickSavedFood.invoke(itemView.context, data)
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<SavedFood>) {
        savedFoodList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemSavedFoodViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSavedFoodBinding.inflate(inflater, parent, false)
        return ItemSavedFoodViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return savedFoodList.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ItemSavedFoodViewHolder, position: Int) {
        val savedFood = savedFoodList[position]
        holder.bind(savedFood)
    }
}

