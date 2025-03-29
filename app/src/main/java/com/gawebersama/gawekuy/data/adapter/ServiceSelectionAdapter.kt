package com.gawebersama.gawekuy.data.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gawebersama.gawekuy.data.dataclass.ServiceSelectionModel
import com.gawebersama.gawekuy.databinding.ItemServicesSelectionBinding

class ServiceSelectionAdapter(
    private val services: MutableList<ServiceSelectionModel>,
    private val onDeleteClick: (Int) -> Unit
) :
    RecyclerView.Adapter<ServiceSelectionAdapter.ServiceSelectionViewHolder>() {

    inner class ServiceSelectionViewHolder(private val binding: ItemServicesSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: ServiceSelectionModel) {
            with(binding) {
                etServiceName.setText(service.name)
                etServicePrice.setText(service.price.toString())

                // Tambahkan TextWatcher untuk menyimpan perubahan input ke dalam list
                etServiceName.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        services[layoutPosition].name = s.toString()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                etServicePrice.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        services[layoutPosition].price = s.toString().toDoubleOrNull() ?: 0.0
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                ivDeleteService.setOnClickListener {
                    onDeleteClick(layoutPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceSelectionViewHolder {
        val binding = ItemServicesSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceSelectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceSelectionViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount(): Int = services.size
}
