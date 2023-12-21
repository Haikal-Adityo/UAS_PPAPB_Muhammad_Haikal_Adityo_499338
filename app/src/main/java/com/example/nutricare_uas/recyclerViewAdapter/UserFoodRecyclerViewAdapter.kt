package com.example.nutricare_uas.recyclerViewAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutricare_uas.databinding.ItemFoodBinding
import com.example.nutricare_uas.userFood.UserFood

class UserFoodRecyclerViewAdapter(
    private var userFoodList: List<UserFood>,
    private val onClickUserFood: (context: Context, UserFood) -> Unit
) : RecyclerView.Adapter<UserFoodRecyclerViewAdapter.ItemUserFoodViewHolder>() {

    inner class ItemUserFoodViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: UserFood) {
            with(binding) {
                txtFoodName.text = data.foodName
                txtCalorie.text = data.calorie.toString()
                txtServing.text = data.serving.toString()
                txtTotalCalorie.text = data.totalCalorie.toString()

                itemView.setOnClickListener {
                    onClickUserFood.invoke(itemView.context, data)
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<UserFood>) {
        userFoodList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemUserFoodViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFoodBinding.inflate(inflater, parent, false)
        return ItemUserFoodViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userFoodList.size
    }

    override fun onBindViewHolder(holder: ItemUserFoodViewHolder, position: Int) {
        val userFood = userFoodList[position]
        holder.bind(userFood)
    }
}