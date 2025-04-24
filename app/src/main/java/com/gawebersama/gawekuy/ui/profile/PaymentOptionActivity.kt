package com.gawebersama.gawekuy.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.datamodel.PaymentInfoModel
import com.gawebersama.gawekuy.data.enum.BankType
import com.gawebersama.gawekuy.data.enum.EwalletType
import com.gawebersama.gawekuy.data.enum.PaymentType
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.ActivityPaymentOptionBinding

class PaymentOptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentOptionBinding
    private val userViewModel by viewModels<UserViewModel>()

    private var selectedPayment: String = ""
    private var paymentInfo: PaymentInfoModel? = null

    companion object {
        const val TAG = "PaymentOptionActivity"
    }

    val bankTypeList = BankType.entries.map { it.name.replace("_", " ") }
    val ewalletTypeList = EwalletType.entries.map { it.name.replace("_", " ") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bankAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bankTypeList)
        val eWalletAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ewalletTypeList)

        with(binding) {
            spinnerBankType.setAdapter(bankAdapter)
            spinnerBankType.setOnItemClickListener { _, _, position, _ ->
                bankTypeList[position]
            }
            spinnerBankType.setDropDownBackgroundResource(R.drawable.dropdown_background)

            spinnerEwalletType.setAdapter(eWalletAdapter)
            spinnerEwalletType.setOnItemClickListener { _, _, position, _ ->
                ewalletTypeList[position]
            }
            spinnerEwalletType.setDropDownBackgroundResource(R.drawable.dropdown_background)
        }

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }

            rgPaymentType.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_bank -> {
                        cvBank.visibility = View.VISIBLE
                        cvEwallet.visibility = View.GONE
                    }

                    R.id.rb_ewallet -> {
                        cvBank.visibility = View.GONE
                        cvEwallet.visibility = View.VISIBLE
                    }
                }
            }

            ccp.registerCarrierNumberEditText(tietEwalletPhoneNumber)

            btnSavePayment.setOnClickListener {
                selectedPayment = if (rgPaymentType.checkedRadioButtonId == R.id.rb_bank) PaymentType.BANK.formatted() else PaymentType.E_WALLET.formatted()

                if (selectedPayment == PaymentType.BANK.formatted()) {
                    val selectedBankType = spinnerBankType.text.toString().trim()
                    val accountNumber = tietBankAccountNumber.text.toString().trim()
                    val accountHolderName = tietBankAccountName.text.toString().trim()

                    if (selectedBankType.isEmpty()) {
                        spinnerBankType.error = "Pilih jenis bank terlebih dahulu"
                        return@setOnClickListener
                    }

                    if (accountNumber.isEmpty()) {
                        tietBankAccountNumber.error = "Nomor rekening tidak boleh kosong"
                        return@setOnClickListener
                    }

                    if (accountHolderName.isEmpty()) {
                        tietBankAccountName.error = "Nama pemilik rekening tidak boleh kosong"
                        return@setOnClickListener
                    }

                    paymentInfo = PaymentInfoModel(
                        paymentType = selectedPayment,
                        bankType = selectedBankType,
                        bankAccountName = accountHolderName,
                        bankAccountNumber = accountNumber,
                        ewalletType = null,
                        ewalletAccountName = null,
                        ewalletNumber = null
                    )
                } else {
                    val selectedEwalletType = spinnerEwalletType.text.toString().trim()
                    val eWalletAccountName = tietEwalletAccountName.text.toString().trim()
                    val eWalletPhoneNumber = ccp.fullNumber.trim()

                    if (selectedEwalletType.isEmpty()) {
                        spinnerEwalletType.error = "Tipe e-wallet tidak boleh kosong"
                        return@setOnClickListener
                    }
                    if (eWalletAccountName.isEmpty()) {
                        tietEwalletAccountName.error = "Nama pemilik e-wallet tidak boleh kosong"
                        return@setOnClickListener
                    }

                    if (eWalletPhoneNumber.isEmpty()) {
                        tietEwalletPhoneNumber.error = "Nomor e-wallet tidak boleh kosong"
                        return@setOnClickListener
                    }

                    paymentInfo = PaymentInfoModel(
                        paymentType = selectedPayment,
                        ewalletType = selectedEwalletType,
                        ewalletAccountName = eWalletAccountName,
                        ewalletNumber = eWalletPhoneNumber,
                        bankType = null,
                        bankAccountName = null,
                        bankAccountNumber = null
                    )
                }

                userViewModel.updatePaymentInfo(paymentInfo)
            }
        }
    }

    private fun observeViewModel() {
        userViewModel.apply {
            paymentType.observe(this@PaymentOptionActivity) { type ->
                if (type != null) {
                    selectedPayment = type
                    when (type) {
                        PaymentType.BANK.formatted() -> {
                            binding.rgPaymentType.check(R.id.rb_bank)
                            binding.cvBank.visibility = View.VISIBLE
                            binding.cvEwallet.visibility = View.GONE
                        }
                        PaymentType.E_WALLET.formatted() -> {
                            binding.rgPaymentType.check(R.id.rb_ewallet)
                            binding.cvBank.visibility = View.GONE
                            binding.cvEwallet.visibility = View.VISIBLE
                        }
                    }
                }
            }

            bankType.observe(this@PaymentOptionActivity) {
                if (bankTypeList.contains(it)) {
                    binding.spinnerBankType.setText(it, false)
                }
            }

            bankAccountName.observe(this@PaymentOptionActivity) {
                binding.tietBankAccountName.setText(it)
            }

            bankAccountNumber.observe(this@PaymentOptionActivity) {
                binding.tietBankAccountNumber.setText(it)
            }

            ewalletType.observe(this@PaymentOptionActivity) {
                if (ewalletTypeList.contains(it)) {
                    binding.spinnerEwalletType.setText(it, false)
                }
            }

            ewalletAccountName.observe(this@PaymentOptionActivity) {
                binding.tietEwalletAccountName.setText(it)
            }

            ewalletNumber.observe(this@PaymentOptionActivity) { phone ->
                val currentCode = binding.ccp.selectedCountryCode
                val phoneWithoutCode = phone?.removePrefix(currentCode)
                binding.tietEwalletPhoneNumber.setText(phoneWithoutCode)
            }
            
            authStatus.observe(this@PaymentOptionActivity) {
                if (it.first) {
                    Toast.makeText(this@PaymentOptionActivity, it.second, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    fun PaymentType.formatted(): String {
        return name.uppercase().replace("_", "-")
    }
}
