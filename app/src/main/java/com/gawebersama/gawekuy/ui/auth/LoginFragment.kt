package com.gawebersama.gawekuy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.viewmodel.AuthViewModel
import com.gawebersama.gawekuy.databinding.BottomSheetDialogLoginBinding
import com.gawebersama.gawekuy.databinding.FragmentLoginBinding
import com.gawebersama.gawekuy.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showBottomSheetDialog()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetDialogLoginBinding.inflate(layoutInflater)

        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setOnShowListener { dialog ->
            (dialog as BottomSheetDialog).window?.setDimAmount(0f)
        }

        bottomSheetDialog.setContentView(sheetBinding.root)
        bottomSheetDialog.show()

        with(sheetBinding) {
            tvRegister.setOnClickListener {
                navigate(R.id.login_to_registerSelect)
                bottomSheetDialog.dismiss()
            }

            btnBack.setOnClickListener {
                navigate(R.id.login_to_onboarding)
                bottomSheetDialog.dismiss()
            }

            btnLogin.setOnClickListener {
                val email = tietEmail.text.toString().trim()
                val password = tietPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    showToast("Email dan Password wajib diisi")
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    authViewModel.loginUser(email, password)
                }
            }

            authViewModel.authStatus.observe(viewLifecycleOwner) { result ->
                if (result.first) {
                    navigateToMainActivity()
                } else {
                    Toast.makeText(activity, result.second, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigate(destination: Int) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        view?.findNavController()?.navigate(destination, null, navOptions)
    }

    private fun navigateToMainActivity() {
        Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
