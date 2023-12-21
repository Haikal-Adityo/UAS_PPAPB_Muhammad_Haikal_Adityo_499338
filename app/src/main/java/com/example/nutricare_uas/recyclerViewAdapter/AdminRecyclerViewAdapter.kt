package com.example.nutricare_uas.recyclerViewAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutricare_uas.databinding.ItemFoodBinding
import com.example.nutricare_uas.model.AdminFood

class AdminRecyclerViewAdapter (
    private var foodList: List<AdminFood>,
    private val onClickFood: (context: Context, AdminFood) -> Unit
) : RecyclerView.Adapter<AdminRecyclerViewAdapter.ItemFoodViewHolder>() {

    inner class ItemFoodViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: AdminFood) {
            with(binding) {
                txtFoodName.text = data.foodName
                txtCalorie.text = data.calorie.toString()
                txtServing.text = data.serving.toString()
                txtTotalCalorie.text = data.totalCalorie?.toString() ?: ""

                itemView.setOnClickListener {
                    onClickFood.invoke(itemView.context, data)
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<AdminFood>) {
        foodList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int)
    : ItemFoodViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFoodBinding.inflate(inflater, parent, false)
        return ItemFoodViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: ItemFoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.bind(food)
    }
}