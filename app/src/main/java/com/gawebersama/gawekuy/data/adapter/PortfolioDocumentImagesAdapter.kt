package com.gawebersama.gawekuy.data.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gawebersama.gawekuy.databinding.ItemPortfolioImagesListBinding

class PortfolioDocumentImagesAdapter(
    private val images: MutableList<Uri>,
    private val onClickListener: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<PortfolioDocumentImagesAdapter.DocImagesViewHolder>() {

    inner class DocImagesViewHolder(private val binding: ItemPortfolioImagesListBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(portfolioImage: Uri) {
            with(binding) {
                tietFileImages.setText(portfolioImage.lastPathSegment)

                btnSelectImages.setOnClickListener {
                    onClickListener(bindingAdapterPosition)
                }

                ivDeletePortfolioImages.setOnClickListener {
                    onDeleteClick(bindingAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocImagesViewHolder {
        val binding = ItemPortfolioImagesListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocImagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocImagesViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}
