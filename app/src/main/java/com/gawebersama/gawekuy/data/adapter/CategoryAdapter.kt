package com.gawebersama.gawekuy.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.data.datamodel.CategoryModel
import com.gawebersama.gawekuy.databinding.ItemCategoryBinding

class CategoryAdapter(private val categoryModelList : List<CategoryModel>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    var selectCategory : OnItemClickListener? = null

    inner class CategoryViewHolder(private val binding : ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryModel: CategoryModel) {
            with (binding) {
                tvCategoryName.text = categoryModel.name
                Glide.with(this.root).load(categoryModel.image).into(ivCategoryImage)

                itemView.setOnClickListener {
                    selectCategory?.onItemClick(categoryModel)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoryModelList[position])
    }

    override fun getItemCount(): Int {
        return categoryModelList.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.selectCategory = listener
    }

    interface OnItemClickListener {
        fun onItemClick(categoryModel: CategoryModel)
    }
}