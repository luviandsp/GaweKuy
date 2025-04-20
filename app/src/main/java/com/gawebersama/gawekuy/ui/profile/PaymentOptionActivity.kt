package com.gawebersama.gawekuy.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.datamodel.PaymentInfoModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
                selectedPayment = if (rgPaymentType.checkedRadioButtonId == R.id.rb_bank) "Bank" else "E-Wallet"

                if (selectedPayment == "Bank") {
                    val bankName = tietBankName.text.toString().uppercase().trim()
                    val accountNumber = tietBankAccountNumber.text.toString().trim()
                    val accountHolderName = tietBankAccountName.text.toString().trim()

                    if (bankName.isEmpty()) {
                        tietBankName.error = "Nama bank tidak boleh kosong"
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
                        bankName = bankName,
                        bankAccountName = accountHolderName,
                        bankAccountNumber = accountNumber,
                        ewalletType = null,
                        ewalletAccountName = null,
                        ewalletNumber = null
                    )
                } else {
                    val eWalletType = tietEwalletType.text.toString().uppercase().trim()
                    val eWalletAccountName = tietEwalletAccountName.text.toString().trim()
                    val eWalletPhoneNumber = ccp.fullNumber.trim()

                    if (eWalletType.isEmpty()) {
                        tietEwalletType.error = "Tipe e-wallet tidak boleh kosong"
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
                        ewalletType = eWalletType,
                        ewalletAccountName = eWalletAccountName,
                        ewalletNumber = eWalletPhoneNumber,
                        bankName = null,
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
                        "Bank" -> {
                            binding.rgPaymentType.check(R.id.rb_bank)
                            binding.cvBank.visibility = View.VISIBLE
                            binding.cvEwallet.visibility = View.GONE
                        }
                        "E-Wallet" -> {
                            binding.rgPaymentType.check(R.id.rb_ewallet)
                            binding.cvBank.visibility = View.GONE
                            binding.cvEwallet.visibility = View.VISIBLE
                        }
                    }
                }
            }

            bankName.observe(this@PaymentOptionActivity) {
                binding.tietBankName.setText(it)
            }

            bankAccountName.observe(this@PaymentOptionActivity) {
                binding.tietBankAccountName.setText(it)
            }

            bankAccountNumber.observe(this@PaymentOptionActivity) {
                binding.tietBankAccountNumber.setText(it)
            }

            ewalletType.observe(this@PaymentOptionActivity) {
                binding.tietEwalletType.setText(it)
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
}
