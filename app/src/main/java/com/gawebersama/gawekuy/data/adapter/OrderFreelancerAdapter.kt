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
import com.gawebersama.gawekuy.data.datamodel.TransactionDetailModel
import com.gawebersama.gawekuy.data.enum.OrderStatus
import com.gawebersama.gawekuy.data.viewmodel.TransactionViewModel
import com.gawebersama.gawekuy.databinding.DialogOrderBinding
import com.gawebersama.gawekuy.databinding.ItemOrderBinding

class OrderFreelancerAdapter(
    private val transactionViewModel: TransactionViewModel
) : ListAdapter<TransactionDetailModel, OrderFreelancerAdapter.OrderFreelancerViewHolder>(DIFF_CALLBACK) {

    inner class OrderFreelancerViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: TransactionDetailModel) {
            with(binding) {
                tvTags.text = order.transaction.statusForFreelancer
                tvServiceName.text = order.service.serviceName
                tvSelectedServiceName.text = order.transaction.selectedServiceType
                tvUserName.text = order.buyer.name

                btnChat.visibility = View.GONE
                btnDoneOrder.visibility = View.GONE
                btnSendProject.visibility = View.GONE
                btnCancelOrder.visibility = View.GONE
                btnRevision.visibility = View.GONE
                llButtonAcceptReject.visibility = View.GONE

                val email = order.buyer.email
                val phone = order.buyer.phone

                when (order.transaction.statusForFreelancer) {
                    OrderStatus.IN_PROGRESS.formatted() -> {
                        updateTagsColor(R.color.light_blue, R.color.dark_blue)
                        btnChat.visibility = View.VISIBLE
                        btnSendProject.visibility = View.VISIBLE
                        btnDoneOrder.visibility = View.VISIBLE
                    }
                    OrderStatus.PENDING.formatted() -> {
                        updateTagsColor(R.color.light_yellow, R.color.dark_yellow)
                        llButtonAcceptReject.visibility = View.VISIBLE
                    }
                    OrderStatus.WAITING_RESPONSES.formatted() -> {
                        updateTagsColor(R.color.light_yellow, R.color.dark_yellow)
                        btnChat.visibility = View.VISIBLE
                        btnSendProject.visibility = View.VISIBLE
                    }
                    OrderStatus.COMPLETED.formatted() -> {
                        updateTagsColor(R.color.light_green, R.color.dark_green)
                    }
                    OrderStatus.REJECTED.formatted() -> {
                        updateTagsColor(R.color.light_red, R.color.dark_red)
                    }
                    OrderStatus.CANCELLED.formatted() -> {
                        updateTagsColor(R.color.light_red, R.color.dark_red)
                    }
                    OrderStatus.REVISION.formatted() -> {
                        updateTagsColor(R.color.light_blue, R.color.dark_blue)
                        btnChat.visibility = View.VISIBLE
                        btnSendProject.visibility = View.VISIBLE
                        btnDoneOrder.visibility = View.VISIBLE
                    }
                    OrderStatus.WAITING_PAYMENT.formatted() -> {
                        updateTagsColor(R.color.light_purple, R.color.dark_purple)
                    }
                    OrderStatus.PAID.formatted() -> {
                        updateTagsColor(R.color.light_green, R.color.dark_green)
                    }
                    else -> { }
                }

                btnAcceptProject.setOnClickListener {
                    showOrderDialog("accept", order)
                }

                btnRejectProject.setOnClickListener {
                    showOrderDialog("reject", order)
                }

                btnDoneOrder.setOnClickListener {
                    showOrderDialog("done", order)
                }

                btnChat.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, "https://wa.me/$phone".toUri())
                    itemView.context.startActivity(intent)
                }

                btnSendProject.setOnClickListener {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        // Set email recipient
                        data = "mailto:${email}".toUri()

                        // Automatically fill subject and text
                        putExtra(Intent.EXTRA_SUBJECT, "Terkait Proyek di Gawein")
                        putExtra(Intent.EXTRA_TEXT, "Halo ${order.buyer.name},\n\nSaya ingin membahas proyek ini lebih lanjut. Terima kasih.")
                    }

                    // Start email app
                    itemView.context.startActivity(Intent.createChooser(intent, "Kirim email dengan"))
                }
            }
        }

        fun updateTagsColor(cardColor: Int, textColor: Int) {
            binding.cvTags.setCardBackgroundColor(ContextCompat.getColor(itemView.context, cardColor))
            binding.tvTags.setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        fun showOrderDialog(sourceButton: String, order: TransactionDetailModel) {
            val dialogBinding = DialogOrderBinding.inflate(LayoutInflater.from(itemView.context), null, false)
            val orderDialog = AlertDialog.Builder(itemView.context).setView(dialogBinding.root).create()

            when (sourceButton) {
                "done" -> {
                    dialogBinding.tvDialogTitle.text = itemView.context.getString(R.string.title_dialog_done_order)
                    dialogBinding.tvDialogMessage.text = itemView.context.getString(R.string.message_dialog_done_order)
                    dialogBinding.btnConfirm.text = itemView.context.getString(R.string.done)
                    dialogBinding.btnCancel.text = itemView.context.getString(R.string.back)
                }
                "accept" -> {
                    dialogBinding.tvDialogTitle.text = itemView.context.getString(R.string.title_dialog_accept_order)
                    dialogBinding.tvDialogMessage.text = itemView.context.getString(R.string.message_dialog_accept_order)
                    dialogBinding.btnConfirm.text = itemView.context.getString(R.string.accept)
                    dialogBinding.btnCancel.text = itemView.context.getString(R.string.back)
                }
                "reject" -> {
                    dialogBinding.tvDialogTitle.text = itemView.context.getString(R.string.title_dialog_reject_order)
                    dialogBinding.tvDialogMessage.text = itemView.context.getString(R.string.message_dialog_reject_order)
                    dialogBinding.btnConfirm.text = itemView.context.getString(R.string.reject)
                    dialogBinding.btnCancel.text = itemView.context.getString(R.string.back)
                }
            }

            with(dialogBinding) {
                btnConfirm.setOnClickListener {
                    when (sourceButton) {
                        "done" -> {
                            transactionViewModel.updateTransactionStatusForBuyer(
                                order.transaction.orderId,
                                OrderStatus.WAITING_RESPONSES.formatted()
                            )

                            transactionViewModel.updateTransactionStatusForFreelancer(
                                order.transaction.orderId,
                                OrderStatus.WAITING_RESPONSES.formatted()
                            )

                            binding.btnDoneOrder.visibility = View.GONE
                            binding.btnChat.visibility = View.VISIBLE
                            binding.btnSendProject.visibility = View.VISIBLE
                        }
                        "accept" -> {
                            transactionViewModel.updateTransactionStatusForBuyer(
                                order.transaction.orderId,
                                OrderStatus.IN_PROGRESS.formatted()
                            )

                            transactionViewModel.updateTransactionStatusForFreelancer(
                                order.transaction.orderId,
                                OrderStatus.IN_PROGRESS.formatted()
                            )

                            binding.llButtonAcceptReject.visibility = View.GONE
                            binding.btnChat.visibility = View.VISIBLE
                            binding.btnSendProject.visibility = View.VISIBLE
                            binding.btnDoneOrder.visibility = View.VISIBLE
                        }
                        "reject" -> {
                            transactionViewModel.updateTransactionStatusForBuyer(
                                order.transaction.orderId,
                                OrderStatus.REJECTED.formatted()
                            )

                            transactionViewModel.updateTransactionStatusForFreelancer(
                                order.transaction.orderId,
                                OrderStatus.REJECTED.formatted()
                            )

                            binding.llButtonAcceptReject.visibility = View.GONE
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderFreelancerViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderFreelancerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderFreelancerViewHolder, position: Int) {
        val order = getItem(position)
        if (order != null) {
            holder.bind(order)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionDetailModel>() {
            override fun areItemsTheSame(oldItem: TransactionDetailModel, newItem: TransactionDetailModel): Boolean {
                return oldItem.transaction.orderId == newItem.transaction.orderId
            }

            override fun areContentsTheSame(oldItem: TransactionDetailModel, newItem: TransactionDetailModel): Boolean {
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