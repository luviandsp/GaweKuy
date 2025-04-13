package com.gawebersama.gawekuy.data.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gawebersama.gawekuy.data.datamodel.TransactionModel
import com.gawebersama.gawekuy.databinding.ItemOrderBinding

class OrderClientAdapter : ListAdapter<TransactionModel, OrderClientAdapter.OrderClientViewHolder>(DIFF_CALLBACK) {

    inner class OrderClientViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: TransactionModel) {
            with(binding) {
                tvTags.text = order.status
                tvServiceName.text = order.serviceName
                tvSelectedServiceName.text = order.selectedServiceType
                tvUserName.text = order.sellerName

                btnSendProject.visibility = View.GONE
                btnChatFreelancer.visibility = View.VISIBLE

                val phone = order.sellerPhone

                btnChatFreelancer.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, "https://wa.me/$phone".toUri())
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderClientViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderClientViewHolder, position: Int) {
        val order = getItem(position)
        if (order != null) {
            holder.bind(order)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionModel>() {
            override fun areItemsTheSame(oldItem: TransactionModel, newItem: TransactionModel): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            override fun areContentsTheSame(oldItem: TransactionModel, newItem: TransactionModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}