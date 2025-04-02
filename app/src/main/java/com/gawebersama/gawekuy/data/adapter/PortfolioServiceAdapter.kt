package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gawebersama.gawekuy.databinding.ItemPortfolioServiceBinding

class PortfolioServiceAdapter(
    private val portfolio: MutableList<Map<String, String>>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<PortfolioServiceAdapter.PortfolioServiceViewHolder>() {

    inner class PortfolioServiceViewHolder(private val binding: ItemPortfolioServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(portfolioData: Map<String, String>) {
            with(binding) {
                val portfolioName = portfolioData["portfolioName"] ?: ""

                tvPortofolioName.text = portfolioName

                ivDeletePortfolioImages.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onDeleteClick(position)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioServiceViewHolder {
        val binding = ItemPortfolioServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PortfolioServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PortfolioServiceViewHolder, position: Int) {
        holder.bind(portfolio[position])
    }

    override fun getItemCount(): Int {
        return portfolio.size
    }
}