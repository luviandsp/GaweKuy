package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.data.adapter.PortfolioImagesAdapter.PortfolioImagesViewHolder
import com.gawebersama.gawekuy.databinding.ItemPortfolioImagesBinding

class PortfolioImagesAdapter(private val imagesList: List<String>) : RecyclerView.Adapter<PortfolioImagesViewHolder>() {

    inner class PortfolioImagesViewHolder(private val binding: ItemPortfolioImagesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(portfolio: String) {
            with(binding) {
                Glide
                    .with(itemView)
                    .load(portfolio)
                    .into(ivPortofolioImages)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioImagesViewHolder {
        val binding = ItemPortfolioImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PortfolioImagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PortfolioImagesViewHolder, position: Int) {
        holder.bind(imagesList[position])
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }
}