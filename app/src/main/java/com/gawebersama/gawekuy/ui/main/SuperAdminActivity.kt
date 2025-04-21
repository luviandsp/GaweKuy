package com.gawebersama.gawekuy.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.OrderSuperAdminAdapter
import com.gawebersama.gawekuy.data.datastore.AppPreferences
import com.gawebersama.gawekuy.data.datastore.LoginPreferences
import com.gawebersama.gawekuy.data.enum.OrderStatus
import com.gawebersama.gawekuy.data.viewmodel.TransactionViewModel
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.ActivitySuperAdminBinding
import com.gawebersama.gawekuy.databinding.DialogLogoutBinding
import com.gawebersama.gawekuy.ui.auth.AuthActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SuperAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperAdminBinding
    private val userViewModel by viewModels<UserViewModel>()
    private val transactionViewModel by viewModels<TransactionViewModel>()

    private lateinit var orderSuperAdminAdapter: OrderSuperAdminAdapter
    private lateinit var loginPreferences: LoginPreferences
    private lateinit var appPreferences: AppPreferences

    private var orderType : String = OrderStatus.ALL.name

    companion object {
        private const val TAG = "SuperAdminActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginPreferences = LoginPreferences(this@SuperAdminActivity)
        appPreferences = AppPreferences(this@SuperAdminActivity)

        lifecycleScope.launch {
            visibilityButton(orderType)
        }

        binding.srlSuperAdmin.setOnRefreshListener {
            refreshOrderData()
        }

        transactionViewModel.getAllTransactions(resetPaging = true)
        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            btnLogout.setOnClickListener {
                showLogoutDialog()
            }

            orderSuperAdminAdapter = OrderSuperAdminAdapter(transactionViewModel)
            rvOrder.apply {
                layoutManager = LinearLayoutManager(this@SuperAdminActivity)
                adapter = orderSuperAdminAdapter
            }

            btnLoadMore.setOnClickListener {
                transactionViewModel.getAllTransactions(filter = orderType, resetPaging = false)
            }

            btnAllOrder.setOnClickListener {
                orderType = OrderStatus.ALL.name
                lifecycleScope.launch {
                    visibilityButton(orderType)
                }
                getTransactionData()
            }

            btnWaitingRefundOrder.setOnClickListener {
                orderType = OrderStatus.WAITING_REFUND.name
                lifecycleScope.launch {
                    visibilityButton(orderType)
                }
                getTransactionData()
            }

            btnCancelledOrder.setOnClickListener {
                orderType = OrderStatus.CANCELLED.name
                lifecycleScope.launch {
                    visibilityButton(orderType)
                }
                getTransactionData()
            }

            btnWatingPaymentOrder.setOnClickListener {
                orderType = OrderStatus.WAITING_PAYMENT.name
                lifecycleScope.launch {
                    visibilityButton(orderType)
                }
                getTransactionData()
            }

            btnPaidOrder.setOnClickListener {
                orderType = OrderStatus.PAID.name
                lifecycleScope.launch {
                    visibilityButton(orderType)
                }
                getTransactionData()
            }

        }
    }

    private fun observeViewModels() {
        transactionViewModel.apply {
            transactionList.observe(this@SuperAdminActivity) { transactions ->

                orderSuperAdminAdapter.submitList(transactions)
                orderSuperAdminAdapter.notifyDataSetChanged()
                Log.d(TAG, "Transaction List: $transactions")

                if (transactions.isEmpty()) {
                    binding.ivPlaceholderEmpty.visibility = View.VISIBLE
                } else {
                    binding.ivPlaceholderEmpty.visibility = View.GONE
                }

                binding.btnLoadMore.visibility = if (transactionViewModel.hasMoreData()) View.VISIBLE else View.GONE
                binding.srlSuperAdmin.isRefreshing = false
            }
        }
    }

    private suspend fun visibilityButton(type : String) {
        with(binding) {
            val isDarkMode = appPreferences.darkModeFlow.first()
            Log.d(TAG, "isDarkMode: $isDarkMode")

            fun setActive(btn: MaterialButton) {
                btn.apply {
                    setTextColor(ContextCompat.getColor(this@SuperAdminActivity, R.color.blue))
                    setStrokeColorResource(R.color.blue)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            this@SuperAdminActivity,
                            if (!isDarkMode) R.color.white else R.color.dark_grey
                        )
                    )
                }
            }

            fun setInactive(btn: MaterialButton) {
                btn.apply {
                    setTextColor(
                        ContextCompat.getColor(
                            this@SuperAdminActivity,
                            if (!isDarkMode) R.color.inactive_color_text else R.color.inactive_color_text_dark
                        )
                    )
                    setStrokeColorResource(
                        if (!isDarkMode) R.color.grey else R.color.inactive_color_dark
                    )
                    setBackgroundColor(
                        ContextCompat.getColor(
                            this@SuperAdminActivity,
                            if (!isDarkMode) R.color.inactive_color else R.color.inactive_color_dark
                        )
                    )
                }
            }

            when (type) {
                OrderStatus.ALL.name -> {
                    setActive(btnAllOrder)
                    setInactive(btnPaidOrder)
                    setInactive(btnWatingPaymentOrder)
                    setInactive(btnWaitingRefundOrder)
                    setInactive(btnCancelledOrder)
                }
                OrderStatus.PAID.name -> {
                    setInactive(btnAllOrder)
                    setActive(btnPaidOrder)
                    setInactive(btnWatingPaymentOrder)
                    setInactive(btnWaitingRefundOrder)
                    setInactive(btnCancelledOrder)
                }
                OrderStatus.WAITING_PAYMENT.name -> {
                    setInactive(btnAllOrder)
                    setInactive(btnPaidOrder)
                    setActive(btnWatingPaymentOrder)
                    setInactive(btnWaitingRefundOrder)
                    setInactive(btnCancelledOrder)
                }
                OrderStatus.WAITING_REFUND.name -> {
                    setInactive(btnAllOrder)
                    setInactive(btnPaidOrder)
                    setInactive(btnWatingPaymentOrder)
                    setActive(btnWaitingRefundOrder)
                    setInactive(btnCancelledOrder)
                }
                OrderStatus.CANCELLED.name -> {
                    setInactive(btnAllOrder)
                    setInactive(btnPaidOrder)
                    setInactive(btnWatingPaymentOrder)
                    setInactive(btnWaitingRefundOrder)
                    setActive(btnCancelledOrder)
                }
                else -> { }
            }
        }
    }

    private fun showLogoutDialog() {
        val dialogBinding = DialogLogoutBinding.inflate(layoutInflater)
        val logoutDialog = AlertDialog.Builder(this@SuperAdminActivity).setView(dialogBinding.root).create()

        with(dialogBinding) {
            btnCancel.setOnClickListener {
                logoutDialog.dismiss()
            }

            btnLogout.setOnClickListener {
                lifecycleScope.launch {
                    loginPreferences.setAdminLoginStatus(false)
                }

                logoutUser()
                logoutDialog.dismiss()
            }

            logoutDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            logoutDialog.show()
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            userViewModel.logoutUser()
            startActivity(Intent(this@SuperAdminActivity, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun getTransactionData() {
        transactionViewModel.getAllTransactions(filter = orderType, resetPaging = true)
    }

    private fun refreshOrderData() {
        binding.srlSuperAdmin.isRefreshing = true

        getTransactionData()
        userViewModel.getUser()

        lifecycleScope.launch {
            binding.srlSuperAdmin.isRefreshing = false
        }
    }
}