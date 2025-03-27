package com.gawebersama.gawekuy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.enum.ClientType
import com.gawebersama.gawekuy.data.viewmodel.AuthViewModel
import com.gawebersama.gawekuy.databinding.BottomSheetDialogRegisterBinding
import com.gawebersama.gawekuy.databinding.FragmentRegisterBinding
import com.gawebersama.gawekuy.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel by viewModels<AuthViewModel>()

    val args: RegisterFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showBottomSheetDialog()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogBinding = BottomSheetDialogRegisterBinding.inflate(layoutInflater)

        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setContentView(dialogBinding.root)
        bottomSheetDialog.show()

        with(dialogBinding) {
            if (args.clientType == ClientType.CLIENT) {
                tvText.setText(R.string.register_text_client)
            } else {
                tvText.setText(R.string.register_text_freelancer)
            }

            ccp.registerCarrierNumberEditText(tietPhone)

            btnRegister.setOnClickListener {
                val email = tietEmail.text.toString().trim()
                val password = tietPassword.text.toString().trim()
                val confirmPassword = tietConfirmPassword.text.toString().trim()
                val fullName = tietFullName.text.toString().trim()
                val phoneNumber = ccp.fullNumber
                val clientType = args.clientType.toString()

                if (!validateInputs(email, password, confirmPassword, fullName, phoneNumber)) {
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    authViewModel.registerUser(email, password, fullName, phoneNumber, clientType)
                }
            }

            authViewModel.authStatus.observe(viewLifecycleOwner) { result ->
                if (result.first) {
                    Toast.makeText(activity, "Registrasi Berhasil, Silakan Login", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(activity, result.second, Toast.LENGTH_SHORT).show()
                }
            }

            tvLogin.setOnClickListener {
                navigateToLogin()
                bottomSheetDialog.dismiss()
            }

            btnBack.setOnClickListener {
                navigateBack()
                bottomSheetDialog.dismiss()
            }

            // Validasi input password secara real-time
            tietPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updatePasswordValidation(s.toString(), dialogBinding)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String, fullName: String, phone: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(activity, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 8) {
            Toast.makeText(activity, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isUpperCase() }) {
            Toast.makeText(activity, "Password harus mengandung huruf kapital", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isLowerCase() }) {
            Toast.makeText(activity, "Password harus mengandung huruf kecil", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!password.any { it.isDigit() }) {
            Toast.makeText(activity, "Password harus mengandung angka", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(activity, "Password tidak sama", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updatePasswordValidation(password: String, binding: BottomSheetDialogRegisterBinding) {
        with(binding) {
            iconLength.setImageResource(if (password.length >= 8) R.drawable.baseline_check_circle_24 else R.drawable.cross_circle)
            iconUppercase.setImageResource(if (password.any { it.isUpperCase() }) R.drawable.baseline_check_circle_24 else R.drawable.cross_circle)
            iconLowercase.setImageResource(if (password.any { it.isLowerCase() }) R.drawable.baseline_check_circle_24 else R.drawable.cross_circle)
            iconNumber.setImageResource(if (password.any { it.isDigit() }) R.drawable.baseline_check_circle_24 else R.drawable.cross_circle)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToLogin() {
        view?.findNavController()?.navigate(R.id.register_to_login)
    }

    private fun navigateBack() {
        view?.findNavController()?.navigate(R.id.register_to_registerSelect)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
