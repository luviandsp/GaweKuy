package com.gawebersama.gawekuy.data.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.datamodel.TransactionModel
import com.gawebersama.gawekuy.data.enum.OrderStatus
import com.gawebersama.gawekuy.data.viewmodel.TransactionViewModel
import com.gawebersama.gawekuy.databinding.DialogOrderBinding
import com.gawebersama.gawekuy.databinding.ItemOrderBinding

class OrderClientAdapter(
    private val transactionViewModel: TransactionViewModel
) : ListAdapter<TransactionModel, OrderClientAdapter.OrderClientViewHolder>(DIFF_CALLBACK) {

    inner class OrderClientViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: TransactionModel) {
            with(binding) {
                tvTags.text = order.statusForBuyer
                tvServiceName.text = order.serviceName
                tvSelectedServiceName.text = order.selectedServiceType
                tvUserName.text = order.sellerName

                // Khusus untuk Freelancer
                btnSendProject.visibility = View.GONE
                llButtonAcceptReject.visibility = View.GONE

                btnChat.visibility = View.VISIBLE
                btnCancelOrder.visibility = View.GONE
                btnDoneOrder.visibility = View.GONE
                btnRevision.visibility = View.GONE

                val phone = order.sellerPhone

                when (order.statusForBuyer) {
                    OrderStatus.IN_PROGRESS.formatted() -> {
                        updateTagsColor(R.color.light_blue, R.color.dark_blue)
                        btnChat.visibility = View.VISIBLE
                    }
                    OrderStatus.COMPLETED.formatted() -> {
                        updateTagsColor(R.color.light_green, R.color.dark_green)
                        btnChat.visibility = View.GONE
                    }
                    OrderStatus.REJECTED.formatted() -> {
                        updateTagsColor(R.color.light_red, R.color.dark_red)
                        btnChat.visibility = View.GONE
                    }
                    OrderStatus.PENDING.formatted() -> {
                        updateTagsColor(R.color.light_yellow, R.color.dark_yellow)
                        btnCancelOrder.visibility = View.VISIBLE
                    }
                    OrderStatus.WAITING_RESPONSES.formatted() -> {
                        updateTagsColor(R.color.light_yellow, R.color.dark_yellow)
                        btnDoneOrder.visibility = View.VISIBLE
                        btnRevision.visibility = View.VISIBLE
                    }
                    OrderStatus.CANCELLED.formatted() -> {
                        updateTagsColor(R.color.light_red, R.color.dark_red)
                        btnChat.visibility = View.GONE
                    }
                    OrderStatus.REVISION.formatted() -> {
                        updateTagsColor(R.color.light_blue, R.color.dark_blue)
                    }
                    else -> { }
                }

                btnDoneOrder.setOnClickListener {
                    showOrderDialog("done", order)
                }

                btnRevision.setOnClickListener {
                    showOrderDialog("revision", order)
                }

                btnCancelOrder.setOnClickListener {
                    showOrderDialog("cancel", order)
                }

                btnChat.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, "https://wa.me/$phone".toUri())
                    itemView.context.startActivity(intent)
                }
            }
        }

        private fun updateTagsColor(cardColor: Int, textColor: Int) {
            binding.cvTags.setCardBackgroundColor(ContextCompat.getColor(itemView.context, cardColor))
            binding.tvTags.setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        fun showOrderDialog(sourceButton: String, order: TransactionModel) {
            val dialogBinding = DialogOrderBinding.inflate(LayoutInflater.from(itemView.context), null, false)
            val orderDialog = AlertDialog.Builder(itemView.context).setView(dialogBinding.root).create()

            when (sourceButton) {
                "done" -> {
                    dialogBinding.tvDialogTitle.text = itemView.context.getString(R.string.title_dialog_done_order)
                    dialogBinding.tvDialogMessage.text = itemView.context.getString(R.string.message_dialog_done_order)
                    dialogBinding.btnConfirm.text = itemView.context.getString(R.string.done)
                    dialogBinding.btnCancel.text = itemView.context.getString(R.string.back)
                }
                "cancel" -> {
                    dialogBinding.tvDialogTitle.text = itemView.context.getString(R.string.title_dialog_cancel_order)
                    dialogBinding.tvDialogMessage.text = itemView.context.getString(R.string.message_dialog_cancel_order)
                    dialogBinding.btnConfirm.text = itemView.context.getString(R.string.cancel_order)
                    dialogBinding.btnCancel.text = itemView.context.getString(R.string.back)
                }
                "revision" -> {
                    dialogBinding.tvDialogTitle.text = itemView.context.getString(R.string.title_dialog_revision_order)
                    dialogBinding.tvDialogMessage.text = itemView.context.getString(R.string.message_dialog_revision_order)
                    dialogBinding.btnConfirm.text = itemView.context.getString(R.string.revision)
                    dialogBinding.btnCancel.text = itemView.context.getString(R.string.back)
                }
            }

            with(dialogBinding) {
                btnConfirm.setOnClickListener {
                    when (sourceButton) {
                        "done" -> {
                            transactionViewModel.updateTransactionStatusForBuyer(order.orderId, OrderStatus.COMPLETED.formatted())
                            transactionViewModel.updateTransactionStatusForFreelancer(order.orderId, OrderStatus.WAITING_PAYMENT.formatted())
                            binding.btnDoneOrder.visibility = View.GONE
                            binding.btnChat.visibility = View.GONE
                            binding.btnRevision.visibility = View.GONE
                        }
                        "cancel" -> {
                            transactionViewModel.updateTransactionStatusForBuyer(
                                order.orderId,
                                OrderStatus.CANCELLED.formatted()
                            )
                            transactionViewModel.updateTransactionStatusForFreelancer(
                                order.orderId,
                                OrderStatus.CANCELLED.formatted()
                            )
                            binding.btnCancelOrder.visibility = View.GONE
                            binding.btnChat.visibility = View.GONE
                        }
                        "revision" -> {
                            transactionViewModel.updateTransactionStatusForBuyer(
                                order.orderId,
                                OrderStatus.REVISION.formatted()
                            )
                            transactionViewModel.updateTransactionStatusForFreelancer(
                                order.orderId,
                                OrderStatus.REVISION.formatted()
                            )
                            binding.btnRevision.visibility = View.GONE
                            binding.btnChat.visibility = View.VISIBLE
                        }
                    }
                    orderDialog.dismiss()
                }

                btnCancel.setOnClickListener {
                    orderDialog.dismiss()
                }

                orderDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                orderDialog.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderClientViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderClientViewHolder, position: Int) {
        val order = getItem(position)
        if (order != null) {
            holder.bind(order)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionModel>() {
            override fun areItemsTheSame(oldItem: TransactionModel, newItem: TransactionModel): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            override fun areContentsTheSame(oldItem: TransactionModel, newItem: TransactionModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun OrderStatus.formatted(): String {
        return name.lowercase().replace("_", " ").capitalizeWords()
    }

    fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}