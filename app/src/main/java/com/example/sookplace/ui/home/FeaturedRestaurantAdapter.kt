package com.example.sookplace.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.sookplace.data.local.entity.FeaturedRestaurantEntity
import com.example.sookplace.data.remote.response.RestaurantItem
import com.example.sookplace.databinding.ItemFeaturedRestaurantBinding

class FeaturedRestaurantAdapter : ListAdapter<FeaturedRestaurantEntity, FeaturedRestaurantAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemFeaturedRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeaturedRestaurantEntity) {
            binding.thumbnail.load(item.thumbnailUrl)
            binding.name.text = item.name
            binding.address.text = item.address
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeaturedRestaurantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FeaturedRestaurantEntity>() {
            override fun areItemsTheSame(oldItem: FeaturedRestaurantEntity, newItem: FeaturedRestaurantEntity): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: FeaturedRestaurantEntity, newItem: FeaturedRestaurantEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}