package com.gawebersama.gawekuy.data.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.PortfolioAdapter.*
import com.gawebersama.gawekuy.data.datamodel.PortfolioModel
import com.gawebersama.gawekuy.databinding.ItemPortfolioBinding

class PortfolioAdapter(
    private val onItemClick: (PortfolioModel) -> Unit,
    private val onHoldClick: (PortfolioModel) -> Unit
) : ListAdapter<PortfolioModel, PortfolioViewHolder>(DIFF_CALLBACK) {

    inner class PortfolioViewHolder(private val binding: ItemPortfolioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(portfolio: PortfolioModel) {

            Log.d("PortfolioAdapter", "Binding portfolio: ${portfolio.portfolioTitle}, Image: ${portfolio.portfolioBannerImage}")

            with(binding) {
                tvTitlePortfolio.text = portfolio.portfolioTitle

                if (!portfolio.portfolioBannerImage.isNullOrEmpty()) {
                    Glide.with(itemView)
                        .load(portfolio.portfolioBannerImage)
                        .into(ivPortfolio)
                } else {
                    Glide.with(itemView)
                        .load(R.drawable.logo_background)
                        .into(ivPortfolio)
                }

                root.setOnClickListener {
                    onItemClick(portfolio)
                }

                root.setOnLongClickListener {
                    onHoldClick(portfolio)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioViewHolder {
        val binding = ItemPortfolioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PortfolioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PortfolioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PortfolioModel>() {
            override fun areItemsTheSame(oldItem: PortfolioModel, newItem: PortfolioModel): Boolean {
                return oldItem.portfolioId == newItem.portfolioId
            }

            override fun areContentsTheSame(oldItem: PortfolioModel, newItem: PortfolioModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
