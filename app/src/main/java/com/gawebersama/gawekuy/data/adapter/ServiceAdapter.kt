package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.datamodel.ServiceModel
import com.gawebersama.gawekuy.data.datamodel.ServiceWithUserModel
import com.gawebersama.gawekuy.databinding.ItemFreelancerServiceBinding
import java.text.DecimalFormat

class ServiceAdapter(private val onItemClick: (ServiceModel) -> Unit) : ListAdapter<ServiceWithUserModel, ServiceAdapter.ServiceViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemFreelancerServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ServiceViewHolder(private val binding: ItemFreelancerServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(serviceWithUser: ServiceWithUserModel) {
            binding.apply {
                val serviceModel = serviceWithUser.service
                val ownerName = serviceWithUser.user.name

                val formatter = DecimalFormat("#,###")
                val cheapPrice = serviceModel.serviceTypes
                    .minOfOrNull { it.price }
                    ?.let { "Rp.${formatter.format(it)}" } ?: "Unknown Price"

                tvTitle.text = serviceModel.serviceName
                Glide.with(itemView)
                    .load(
                        if (serviceModel.imageBannerUrl.isNullOrEmpty()) {
                            R.drawable.logo_background
                        } else {
                            serviceModel.imageBannerUrl
                        }
                    )
                    .into(ivFreelancer)
                tvName.text = ownerName
                tvPrice.text = cheapPrice
                tvRating.text = serviceModel.serviceRating.toString()

                root.setOnClickListener { onItemClick(serviceModel) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ServiceWithUserModel>() {
            override fun areItemsTheSame(oldItem: ServiceWithUserModel, newItem: ServiceWithUserModel): Boolean {
                return oldItem.service.serviceId == newItem.service.serviceId
            }

            override fun areContentsTheSame(oldItem: ServiceWithUserModel, newItem: ServiceWithUserModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}

