package com.gawebersama.gawekuy.data.adapter

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.datamodel.TransactionDetailModel
import com.gawebersama.gawekuy.data.enum.OrderStatus
import com.gawebersama.gawekuy.data.enum.PaymentType
import com.gawebersama.gawekuy.data.viewmodel.TransactionViewModel
import com.gawebersama.gawekuy.databinding.DialogDetailOrderBinding
import com.gawebersama.gawekuy.databinding.DialogOrderBinding
import com.gawebersama.gawekuy.databinding.ItemOrderAdminBinding
import java.text.DecimalFormat

class OrderSuperAdminAdapter(
    private val transactionViewModel: TransactionViewModel
) : ListAdapter<TransactionDetailModel, OrderSuperAdminAdapter.OrderSuperAdminViewHolder>(DIFF_CALLBACK) {

    inner class OrderSuperAdminViewHolder(private val binding: ItemOrderAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: TransactionDetailModel) {
            with(binding) {
                tvTags.text = if (order.transaction.statusForBuyer == OrderStatus.WAITING_REFUND.formatted()) {
                    OrderStatus.WAITING_REFUND.formatted()
                } else if (order.transaction.statusForFreelancer == OrderStatus.WAITING_PAYMENT.formatted()) {
                    OrderStatus.WAITING_PAYMENT.formatted()
                } else if (order.transaction.statusForFreelancer == OrderStatus.PAID.formatted()) {
                    OrderStatus.PAID.formatted()
                } else {
                    OrderStatus.CANCELLED.formatted()
                }

                tvServiceName.text = order.service.serviceName
                tvSelectedServiceName.text = order.transaction.selectedServiceType
                tvUserName.text = if (order.transaction.statusForBuyer == OrderStatus.WAITING_REFUND.formatted() || order.transaction.statusForBuyer == OrderStatus.CANCELLED.formatted()) {
                    order.buyer.name
                } else {
                    order.seller.name
                }

                when (order.transaction.statusForBuyer) {
                    OrderStatus.WAITING_REFUND.formatted() -> {
                        updateTagsColor(R.color.light_yellow, R.color.dark_yellow)
                        btnDoneOrder.visibility = View.VISIBLE
                    }
                    OrderStatus.CANCELLED.formatted() -> {
                        updateTagsColor(R.color.light_red, R.color.dark_red)
                        btnDoneOrder.visibility = View.GONE
                    }
                    else -> { }
                }

                when (order.transaction.statusForFreelancer) {
                    OrderStatus.WAITING_PAYMENT.formatted() -> {
                        updateTagsColor(R.color.light_purple, R.color.dark_purple)
                        btnDoneOrder.visibility = View.VISIBLE
                    }
                    OrderStatus.PAID.formatted() -> {
                        updateTagsColor(R.color.light_green, R.color.dark_green)
                        btnDoneOrder.visibility = View.GONE
                    }
                    else -> { }
                }

                btnDetailOrder.setOnClickListener {
                    if (order.transaction.statusForBuyer == OrderStatus.WAITING_REFUND.formatted() || order.transaction.statusForBuyer == OrderStatus.CANCELLED.formatted()) {
                        showDetailOrderDialog(order, isFreelancer = false)
                    } else if (order.transaction.statusForFreelancer == OrderStatus.WAITING_PAYMENT.formatted() || order.transaction.statusForFreelancer == OrderStatus.PAID.formatted()) {
                        showDetailOrderDialog(order, isFreelancer = true)
                    }
                }

                btnDoneOrder.setOnClickListener {
                    showOrderDialog(order)
                }
            }
        }

        private fun showDetailOrderDialog(order : TransactionDetailModel, isFreelancer : Boolean) {
            val dialogBinding = DialogDetailOrderBinding.inflate(LayoutInflater.from(itemView.context), null, false)
            val orderDetailDialog = AlertDialog.Builder(itemView.context).setView(dialogBinding.root).create()

            with(dialogBinding) {
                val formatter = DecimalFormat("#,###")

                tvOrderId.text = order.transaction.orderId
                tvOrderId.setOnLongClickListener {
                    copyToClipboard(tvOrderId.text.toString())
                    Toast.makeText(itemView.context, "Order ID disalin", Toast.LENGTH_SHORT).show()
                    true
                }

                tvServiceName.text = order.service.serviceName
                tvServiceName.setOnLongClickListener {
                    copyToClipboard(tvServiceName.text.toString())
                    Toast.makeText(itemView.context, "Nama layanan disalin", Toast.LENGTH_SHORT).show()
                    true
                }

                tvSelectedServiceName.text = order.transaction.selectedServiceType
                tvSelectedServiceName.setOnLongClickListener {
                    copyToClipboard(tvSelectedServiceName.text.toString())
                    Toast.makeText(itemView.context, "Jenis layanan disalin", Toast.LENGTH_SHORT).show()
                    true
                }

                tvUserName.text = if (isFreelancer) {
                    order.seller.name
                } else {
                    order.buyer.name
                }
                tvUserName.setOnLongClickListener {
                    copyToClipboard(tvUserName.text.toString())
                    Toast.makeText(itemView.context, "Nama pengguna disalin", Toast.LENGTH_SHORT).show()
                    true
                }

                tvServicePrice.setOnLongClickListener {
                    copyToClipboard(tvServicePrice.text.toString())
                    Toast.makeText(itemView.context, "Harga layanan disalin", Toast.LENGTH_SHORT).show()
                    true
                }

                if (isFreelancer) {
                    val sellerPaymentInfo = order.seller.paymentInfo
                    tvUserRole.text = itemView.context.getString(R.string.freelancer)

                    tvServicePrice.text = itemView.context.getString(R.string.price_format, formatter.format(order.transaction.selectedServicePrice))

                    trPaymentType.visibility = View.VISIBLE
                    tvPaymentType.text = sellerPaymentInfo?.paymentType

                    if (sellerPaymentInfo?.paymentType == PaymentType.BANK.formatted()) {
                        trBankName.visibility = View.VISIBLE
                        trBankAccountName.visibility = View.VISIBLE
                        trBankAccountNumber.visibility = View.VISIBLE

                        trEwalletType.visibility = View.GONE
                        trEwalletAccountName.visibility = View.GONE
                        trEwalletNumber.visibility = View.GONE

                        tvBankName.text = sellerPaymentInfo.bankName
                        tvBankAccountName.text = sellerPaymentInfo.bankAccountName
                        tvBankAccountNumber.text = sellerPaymentInfo.bankAccountNumber
                        tvBankAccountNumber.setOnLongClickListener {
                            copyToClipboard(tvBankAccountNumber.text.toString())
                            Toast.makeText(itemView.context, "Nomor rekening disalin", Toast.LENGTH_SHORT).show()
                            true
                        }
                    } else if (sellerPaymentInfo?.paymentType == PaymentType.E_WALLET.formatted()) {
                        trEwalletType.visibility = View.VISIBLE
                        trEwalletAccountName.visibility = View.VISIBLE
                        trEwalletNumber.visibility = View.VISIBLE

                        trBankName.visibility = View.GONE
                        trBankAccountName.visibility = View.GONE
                        trBankAccountNumber.visibility = View.GONE

                        tvEwalletType.text = sellerPaymentInfo.ewalletType
                        tvEwalletAccountName.text = sellerPaymentInfo.ewalletAccountName
                        tvEwalletNumber.text = sellerPaymentInfo.ewalletNumber
                        tvEwalletNumber.setOnLongClickListener {
                            copyToClipboard(tvEwalletNumber.text.toString())
                            Toast.makeText(itemView.context, "Nomor E-Wallet disalin", Toast.LENGTH_SHORT).show()
                            true
                        }
                    }
                } else {
                    tvUserRole.text = itemView.context.getString(R.string.client)
                    tvServicePrice.text = itemView.context.getString(R.string.price_format, formatter.format(order.transaction.grossAmount))

                    trBankName.visibility = View.GONE
                    trBankAccountName.visibility = View.GONE
                    trBankAccountNumber.visibility = View.GONE
                    trEwalletType.visibility = View.GONE
                    trEwalletAccountName.visibility = View.GONE
                    trEwalletNumber.visibility = View.GONE
                    trPaymentType.visibility = View.GONE
                }

                btnBack.setOnClickListener {
                    orderDetailDialog.dismiss()
                }

                orderDetailDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                orderDetailDialog.show()
            }
        }

        private fun copyToClipboard(text: String) {
            val clipboard = ContextCompat.getSystemService(itemView.context, ClipboardManager::class.java)
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard?.setPrimaryClip(clip)
        }

        fun updateTagsColor(cardColor: Int, textColor: Int) {
            binding.cvTags.setCardBackgroundColor(ContextCompat.getColor(itemView.context, cardColor))
            binding.tvTags.setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

        fun showOrderDialog(order: TransactionDetailModel) {
            val dialogBinding = DialogOrderBinding.inflate(LayoutInflater.from(itemView.context), null, false)
            val orderDialog = AlertDialog.Builder(itemView.context).setView(dialogBinding.root).create()

            with(dialogBinding) {
                tvDialogTitle.text = itemView.context.getString(R.string.title_dialog_done_order)
                tvDialogMessage.text = if (order.transaction.statusForFreelancer == OrderStatus.WAITING_PAYMENT.formatted()) {
                    itemView.context.getString(R.string.message_dialog_paid_order)
                } else if (order.transaction.statusForBuyer == OrderStatus.WAITING_REFUND.formatted()) {
                    itemView.context.getString(R.string.message_dialog_refund_order)
                } else {
                    itemView.context.getString(R.string.message_dialog_done_order)
                }
                btnConfirm.text = itemView.context.getString(R.string.done)
                btnCancel.text = itemView.context.getString(R.string.back)

                btnConfirm.setOnClickListener {
                    if (order.transaction.statusForFreelancer == OrderStatus.WAITING_PAYMENT.formatted()) {
                        transactionViewModel.updateTransactionStatusForFreelancer(
                            order.transaction.orderId,
                            OrderStatus.PAID.formatted()
                        )
                    } else if (order.transaction.statusForBuyer == OrderStatus.WAITING_REFUND.formatted()) {
                        transactionViewModel.updateTransactionStatusForBuyer(
                            order.transaction.orderId,
                            OrderStatus.CANCELLED.formatted()
                        )
                    }

                    orderDialog.dismiss()
                    binding.btnDoneOrder.visibility = View.GONE
                }

                btnCancel.setOnClickListener {
                    orderDialog.dismiss()
                }

                orderDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                orderDialog.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSuperAdminViewHolder {
        val binding = ItemOrderAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderSuperAdminViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderSuperAdminViewHolder, position: Int) {
        val order = getItem(position)
        if (order != null) {
            holder.bind(order)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    companion object {
        private const val TAG = "OrderSuperAdminAdapter"

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

    fun PaymentType.formatted(): String {
        return name.uppercase().replace("_", "-")
    }

    fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}