package com.example.sookplace.ui.search.restaurantDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sookplace.data.remote.response.MenuItem
import com.example.sookplace.databinding.ItemRestaurantMenuBinding
import java.text.DecimalFormat

class MenuRVAdapter : ListAdapter<MenuItem, MenuRVAdapter.MenuViewHolder>(MenuDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemRestaurantMenuBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MenuViewHolder(private val binding: ItemRestaurantMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuItem) {
            binding.textMenuName.text = item.name
            val decimalFormat = DecimalFormat("#,###")
            binding.textMenuPrice.text = "${decimalFormat.format(item.price)}원"
        }
    }

    companion object {
        private val MenuDiffCallback = object : DiffUtil.ItemCallback<MenuItem>() {
            override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}