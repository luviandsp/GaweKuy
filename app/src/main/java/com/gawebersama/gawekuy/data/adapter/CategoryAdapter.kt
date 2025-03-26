package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.data.dataclass.Category
import com.gawebersama.gawekuy.databinding.CategoryItemBinding

class CategoryAdapter(private val categoryList : List<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    var selectCategory : OnItemClickListener? = null

    inner class CategoryViewHolder(private val binding : CategoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            with (binding) {
                tvCategoryName.text = category.name
                Glide.with(this.root).load(category.image).into(ivCategoryImage)

                itemView.setOnClickListener {
                    selectCategory?.onItemClick(category)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoryList[position])
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.selectCategory = listener
    }

    interface OnItemClickListener {
        fun onItemClick(category: Category)
    }
}