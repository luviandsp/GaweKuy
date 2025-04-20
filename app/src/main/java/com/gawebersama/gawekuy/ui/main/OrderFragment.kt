package com.gawebersama.gawekuy.ui.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.OrderClientAdapter
import com.gawebersama.gawekuy.data.adapter.OrderFreelancerAdapter
import com.gawebersama.gawekuy.data.datastore.AppPreferences
import com.gawebersama.gawekuy.data.enum.OrderStatus
import com.gawebersama.gawekuy.data.enum.UserRole
import com.gawebersama.gawekuy.data.viewmodel.TransactionViewModel
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.FragmentOrderBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OrderFragment : Fragment() {

    private var _binding : FragmentOrderBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by viewModels<UserViewModel>()
    private val transactionViewModel by viewModels<TransactionViewModel>()
    private lateinit var appPreferences: AppPreferences
    private lateinit var orderClientAdapter: OrderClientAdapter
    private lateinit var orderFreelancerAdapter: OrderFreelancerAdapter

    private var orderType : String = OrderStatus.ALL.name
    private var isClient : Boolean = true
    private var buyerId : String = ""
    private var sellerId : String = ""

    companion object {
        const val TAG = "OrderFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appPreferences = AppPreferences(requireContext())

        lifecycleScope.launch {
            visibilityButton(orderType)
        }

        binding.srlOrder.setOnRefreshListener {
            refreshOrderData()
        }
        userViewModel.getUser()

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            orderClientAdapter = OrderClientAdapter(transactionViewModel)
            orderFreelancerAdapter = OrderFreelancerAdapter(transactionViewModel)

            rvOrder.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = orderClientAdapter
            }

            btnLoadMore.setOnClickListener {
                if (isClient) {
                    transactionViewModel.getAllTransactionsByBuyerId(buyerId, filter = orderType, resetPaging = false)
                } else {
                    transactionViewModel.getAllTransactionsBySellerId(sellerId, filter = orderType, resetPaging = false)
                }
            }

            btnClient.apply {
                lifecycleScope.launch {
                    background = createButtonDrawable(isActive = true, isRight = false)
                }
                setTextColor(getColor(requireContext(), R.color.blue))
                setOnClickListener {
                    isClient = true
                    lifecycleScope.launch {
                        updateRoleButton(true)
                    }

                    if (buyerId.isNotEmpty()) {
                        getTransactionData()
                    } else {
                        Log.e(TAG, "Buyer ID is empty")
                    }
                }
            }

            btnFreelancer.apply {
                lifecycleScope.launch {
                    background = createButtonDrawable(false, true)
                }
                setTextColor(getColor(requireContext(), R.color.inactive_color_text))
                setOnClickListener {
                    isClient = false
                    lifecycleScope.launch {
                        updateRoleButton(false)
                    }

                    if (sellerId.isNotEmpty()) {
                        getTransactionData()
                    } else {
                        Log.e(TAG, "Seller ID is empty")
                    }
                }
            }

            btnAllOrder.setOnClickListener {
                orderType = OrderStatus.ALL.name

                lifecycleScope.launch {
                    visibilityButton(orderType)
                }

                getTransactionData()
            }

            btnInProgressOrder.setOnClickListener {
                orderType = OrderStatus.IN_PROGRESS.name

                lifecycleScope.launch {
                    visibilityButton(orderType)
                }

                getTransactionData()
            }

            btnDoneOrder.setOnClickListener {
                orderType = OrderStatus.COMPLETED.name
                lifecycleScope.launch {
                    visibilityButton(orderType)
                }

                getTransactionData()
            }

            btnRevisionOrder.setOnClickListener {
                orderType = OrderStatus.REVISION.name
                lifecycleScope.launch {
                    visibilityButton(orderType)
                }
                getTransactionData()
            }

            btnWaitingOrder.setOnClickListener {
                orderType = OrderStatus.WAITING_RESPONSES.name
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

    private fun getTransactionData() {
        if (isClient) {
            binding.rvOrder.adapter = orderClientAdapter
            transactionViewModel.getAllTransactionsByBuyerId(buyerId, orderType, resetPaging = true)
        } else {
            binding.rvOrder.adapter = orderFreelancerAdapter
            transactionViewModel.getAllTransactionsBySellerId(sellerId, orderType, resetPaging = true)
        }

        Log.d(TAG, "Buyer ID: $buyerId")
        Log.d(TAG, "Seller ID: $sellerId")
        Log.d(TAG, "Order Type: $orderType")
        Log.d(TAG, "Is Client: $isClient")
    }

    private suspend fun updateRoleButton(isClient: Boolean) {
        with(binding) {
            if (isClient) {
                btnClient.background = createButtonDrawable(true, false)
                btnClient.setTextColor(getColor(requireContext(), R.color.blue))

                btnFreelancer.background = createButtonDrawable(false, true)
                btnFreelancer.setTextColor(getColor(requireContext(), R.color.inactive_color_text))

                val layoutParams = btnDoneOrder.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginEnd = (16 * resources.displayMetrics.density).toInt()
                btnDoneOrder.layoutParams = layoutParams

                btnPaidOrder.visibility = View.GONE
                btnWatingPaymentOrder.visibility = View.GONE
            } else {
                btnFreelancer.background = createButtonDrawable(true, true)
                btnFreelancer.setTextColor(getColor(requireContext(), R.color.blue))

                btnClient.background = createButtonDrawable(false, false)
                btnClient.setTextColor(getColor(requireContext(), R.color.inactive_color_text))

                val layoutParams = btnDoneOrder.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginEnd = 0
                btnDoneOrder.layoutParams = layoutParams

                btnPaidOrder.visibility = View.VISIBLE
                btnWatingPaymentOrder.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun createButtonDrawable(isActive: Boolean, isRight: Boolean): MaterialShapeDrawable {
        val isDarkMode = appPreferences.darkModeFlow.first()

        val shape = ShapeAppearanceModel.Builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, if (isRight) 0f else 32f)
            .setBottomLeftCorner(CornerFamily.ROUNDED, if (isRight) 0f else 32f)
            .setTopRightCorner(CornerFamily.ROUNDED, if (isRight) 32f else 0f)
            .setBottomRightCorner(CornerFamily.ROUNDED, if (isRight) 32f else 0f)
            .build()

        return MaterialShapeDrawable(shape).apply {
            fillColor = ColorStateList.valueOf(getColor(requireContext(),
                if (isActive && !isDarkMode) R.color.white
                else if (!isActive && !isDarkMode) R.color.inactive_color
                else if (isActive) R.color.dark_grey
                else R.color.inactive_color_dark)
            )
            setStroke(4f, getColor(requireContext(),
                if (isActive && !isDarkMode) R.color.blue
                else if (!isActive && !isDarkMode) R.color.grey
                else if (isActive) R.color.blue
                else R.color.inactive_color_dark)
            )
        }
    }

    private suspend fun visibilityButton(type : String) {
        with(binding) {
            val isDarkMode = appPreferences.darkModeFlow.first()
            Log.d(TAG, "isDarkMode: $isDarkMode")

            fun setActive(btn: MaterialButton) {
                btn.apply {
                    setTextColor(getColor(requireContext(), R.color.blue))
                    setStrokeColorResource(R.color.blue)
                    setBackgroundColor(
                        getColor(requireContext(), if (!isDarkMode) R.color.white else R.color.dark_grey)
                    )
                }
            }

            fun setInactive(btn: MaterialButton) {
                btn.apply {
                    setTextColor(
                        getColor(requireContext(), if (!isDarkMode) R.color.inactive_color_text else R.color.inactive_color_text_dark)
                    )
                    setStrokeColorResource(
                        if (!isDarkMode) R.color.grey else R.color.inactive_color_dark
                    )
                    setBackgroundColor(
                        getColor(requireContext(), if (!isDarkMode) R.color.inactive_color else R.color.inactive_color_dark)
                    )
                }
            }

            when (type) {
                OrderStatus.ALL.name -> {
                    setActive(btnAllOrder)
                    setInactive(btnInProgressOrder)
                    setInactive(btnRevisionOrder)
                    setInactive(btnPaidOrder)
                    setInactive(btnWaitingOrder)
                    setInactive(btnWatingPaymentOrder)
                    setInactive(btnDoneOrder)
                }
                OrderStatus.IN_PROGRESS.name -> {
                    setInactive(btnAllOrder)
                    setActive(btnInProgressOrder)
                    setInactive(btnRevisionOrder)
                    setInactive(btnPaidOrder)
                    setInactive(btnWaitingOrder)
                    setInactive(btnWatingPaymentOrder)
                    setInactive(btnDoneOrder)
                }
                OrderStatus.REVISION.name -> {
                    setInactive(btnAllOrder)
                    setInactive(btnInProgressOrder)
                    setActive(btnRevisionOrder)
                    setInactive(btnPaidOrder)
                    setInactive(btnWaitingOrder)
                    setInactive(btnWatingPaymentOrder)
                    setInactive(btnDoneOrder)
                }
                OrderStatus.PAID.name -> {
                    setInactive(btnAllOrder)
                    setInactive(btnInProgressOrder)
                    setInactive(btnRevisionOrder)
                    setActive(btnPaidOrder)
                    setInactive(btnWaitingOrder)
                    setInactive(btnWatingPaymentOrder)
                    setInactive(btnDoneOrder)
                }
                OrderStatus.WAITING_RESPONSES.name -> {
                    setInactive(btnAllOrder)
                    setInactive(btnInProgressOrder)
                    setInactive(btnRevisionOrder)
                    setInactive(btnPaidOrder)
                    setActive(btnWaitingOrder)
                    setInactive(btnWatingPaymentOrder)
                    setInactive(btnDoneOrder)
                }
                OrderStatus.WAITING_PAYMENT.name -> {
                    setInactive(btnAllOrder)
                    setInactive(btnInProgressOrder)
                    setInactive(btnRevisionOrder)
                    setInactive(btnPaidOrder)
                    setInactive(btnWaitingOrder)
                    setActive(btnWatingPaymentOrder)
                    setInactive(btnDoneOrder)
                }
                OrderStatus.COMPLETED.name -> {
                    setInactive(btnAllOrder)
                    setInactive(btnInProgressOrder)
                    setInactive(btnRevisionOrder)
                    setInactive(btnPaidOrder)
                    setInactive(btnWaitingOrder)
                    setInactive(btnWatingPaymentOrder)
                    setActive(btnDoneOrder)
                }
                else -> { }
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            userId.observe(viewLifecycleOwner) { id ->
                if (id != null) {
                    buyerId = id
                    sellerId = id

                    getTransactionData()
                }
            }

            userRole.observe(viewLifecycleOwner) { role ->
                if (role != UserRole.FREELANCER.toString()) {
                    binding.btnFreelancer.apply {
                        isClickable = false
                        icon = ContextCompat.getDrawable(requireContext(), R.drawable.lock_icon)
                        iconTint = ColorStateList.valueOf(getColor(requireContext(), R.color.grey))
                    }
                }
            }
        }

        transactionViewModel.apply {
            transactionList.observe(viewLifecycleOwner) { transactions ->
                if (isClient) {
                    orderClientAdapter.submitList(transactions)
                    orderClientAdapter.notifyDataSetChanged()
                } else {
                    orderFreelancerAdapter.submitList(transactions)
                    orderFreelancerAdapter.notifyDataSetChanged()
                }

                if (transactions.isEmpty()) {
                    binding.ivPlaceholderEmpty.visibility = View.VISIBLE
                } else {
                    binding.ivPlaceholderEmpty.visibility = View.GONE
                }

                binding.btnLoadMore.visibility = if (transactionViewModel.hasMoreData()) View.VISIBLE else View.GONE
                binding.srlOrder.isRefreshing = false
            }
        }
    }

    private fun refreshOrderData() {
        binding.srlOrder.isRefreshing = true

        getTransactionData()
        userViewModel.getUser()

        lifecycleScope.launch {
            binding.srlOrder.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        userViewModel.getUserRole()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}