package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.data.dataclass.FreelancerService
import com.gawebersama.gawekuy.databinding.FreelancerItemBinding

class FreelancerServiceAdapter(private val freelancerServiceList: List<FreelancerService>) : RecyclerView.Adapter<FreelancerServiceAdapter.FreelancerServiceViewHolder>() {

    var selectService : OnItemClickListener? = null
    private val limitedList = freelancerServiceList.take(3)

    inner class FreelancerServiceViewHolder(private val binding: FreelancerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(freelancer: FreelancerService) {
            with(binding) {
                tvName.text = freelancer.name
                tvPrice.text = String.format("Rp %,d", freelancer.price?.toInt())
                tvTitle.text = freelancer.title
                tvRating.text = freelancer.rating.toString()
                Glide.with(itemView.context).load(freelancer.image).into(ivFreelancer)

                itemView.setOnClickListener {
                    selectService?.onItemClick(freelancer)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FreelancerServiceViewHolder {
        val binding = FreelancerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FreelancerServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FreelancerServiceViewHolder, position: Int) {
        holder.bind(limitedList[position])
    }

    override fun getItemCount(): Int {
        return limitedList.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.selectService = listener
    }

    interface OnItemClickListener {
        fun onItemClick(service: FreelancerService)
    }
}