package com.gawebersama.gawekuy.data.util

import androidx.recyclerview.widget.DiffUtil
import com.gawebersama.gawekuy.data.datamodel.ServiceSelectionModel

class ServiceTypeDiffCallback(
    private val oldList: List<ServiceSelectionModel>,
    private val newList: List<ServiceSelectionModel>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
