package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.data.dataclass.ServiceModel
import com.gawebersama.gawekuy.databinding.ItemFreelancerServiceBinding

class ServiceAdapter(private val onItemClick: (ServiceModel) -> Unit) :
    ListAdapter<ServiceModel, ServiceAdapter.ServiceViewHolder>(ServiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemFreelancerServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = getItem(position)
        holder.bind(service)
    }

    inner class ServiceViewHolder(private val binding: ItemFreelancerServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(serviceModel: ServiceModel) {
            binding.apply {
                tvTitle.text = serviceModel.serviceName
                Glide.with(itemView.context).load(serviceModel.imageBannerUrl).into(ivFreelancer)

                root.setOnClickListener { onItemClick(serviceModel) }
            }
        }
    }

    class ServiceDiffCallback : DiffUtil.ItemCallback<ServiceModel>() {
        override fun areItemsTheSame(oldItem: ServiceModel, newItem: ServiceModel): Boolean {
            return oldItem.serviceId == newItem.serviceId
        }

        override fun areContentsTheSame(oldItem: ServiceModel, newItem: ServiceModel): Boolean {
            return oldItem == newItem
        }
    }
}
