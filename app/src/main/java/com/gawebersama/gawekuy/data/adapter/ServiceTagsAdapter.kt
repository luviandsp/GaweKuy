package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gawebersama.gawekuy.databinding.ItemServiceTagsBinding

class ServiceTagsAdapter(
    private val tags: MutableList<String>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ServiceTagsAdapter.TagViewHolder>() {
    inner class TagViewHolder(private val binding: ItemServiceTagsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: String) {
            binding.tvTags.text = tag

            binding.tvTags.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemServiceTagsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.bind(tag)
    }

    override fun getItemCount(): Int = tags.size
}