package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel
import com.gawebersama.gawekuy.databinding.ItemServiceSelectedBinding
import java.text.DecimalFormat

class ServiceSelectedAdapter(
    private val services: MutableList<ServiceSelectionModel>,
    private val onRadioButtonClick: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<ServiceSelectedAdapter.ServiceSelectedViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class ServiceSelectedViewHolder(private val binding: ItemServiceSelectedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: ServiceSelectionModel, position: Int) {
            with(binding) {
                tvServiceName.text = service.name

                val formatter = DecimalFormat("#,###")
                val price = service.price
                tvServicePrice.text = itemView.context.getString(R.string.price_format, formatter.format(price))

                rbServiceSelect.isChecked = (position == selectedPosition)

                rbServiceSelect.setOnClickListener {
                    if (selectedPosition != position) {
                        val previousPosition = selectedPosition
                        selectedPosition = position

                        notifyItemChanged(previousPosition)
                        notifyItemChanged(selectedPosition)

                        onRadioButtonClick(position, true)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceSelectedViewHolder {
        val binding = ItemServiceSelectedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceSelectedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceSelectedViewHolder, position: Int) {
        holder.bind(services[position], position)
    }

    override fun getItemCount(): Int = services.size
}
