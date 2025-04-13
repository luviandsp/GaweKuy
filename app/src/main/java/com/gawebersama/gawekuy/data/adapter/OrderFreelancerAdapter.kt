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

class OrderFreelancerAdapter : ListAdapter<TransactionModel, OrderFreelancerAdapter.OrderFreelancerViewHolder>(DIFF_CALLBACK) {

    inner class OrderFreelancerViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: TransactionModel) {
            with(binding) {
                tvTags.text = order.status
                tvServiceName.text = order.serviceName
                tvSelectedServiceName.text = order.selectedServiceType
                tvUserName.text = order.buyerName

                btnSendProject.visibility = View.VISIBLE
                btnChatFreelancer.visibility = View.GONE

                val email = order.buyerEmail

                btnSendProject.setOnClickListener {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        // Set email recipient
                        data = "mailto:${email}".toUri()

                        // Automatically fill subject and text
                        putExtra(Intent.EXTRA_SUBJECT, "Terkait Proyek di Gawein")
                        putExtra(Intent.EXTRA_TEXT, "Halo ${order.buyerName},\n\nSaya ingin membahas proyek ini lebih lanjut. Terima kasih.")
                    }

                    // Start email app
                    itemView.context.startActivity(Intent.createChooser(intent, "Kirim email dengan"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderFreelancerViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderFreelancerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderFreelancerViewHolder, position: Int) {
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