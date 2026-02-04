package com.example.sookplace.ui.search.restaurantDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.sookplace.R
import com.example.sookplace.databinding.ItemImageSliderBinding

class ImageSliderAdapter (private val images: List<String>) :
    RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageSliderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.binding.ivSliderImage.load(images[position]) {
            crossfade(true)
            placeholder(R.drawable.background_radius_gray) // 로딩 중 기본 이미지
        }
    }

    override fun getItemCount(): Int = images.size
}