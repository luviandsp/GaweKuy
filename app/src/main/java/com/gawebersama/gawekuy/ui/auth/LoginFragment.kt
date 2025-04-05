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
import com.gawebersama.gawekuy.data.datastore.LoginPreferences
import com.gawebersama.gawekuy.data.datastore.UserAccountPreferences
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.BottomSheetDialogLoginBinding
import com.gawebersama.gawekuy.databinding.FragmentLoginBinding
import com.gawebersama.gawekuy.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var loginPreferences: LoginPreferences

    private var email = ""
    private var password = ""

    private lateinit var userAccountPreferences: UserAccountPreferences

    companion object {
        const val TAG = "LoginFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginPreferences = LoginPreferences(requireContext())
        userAccountPreferences = UserAccountPreferences(requireContext())

        view.post {
            if (isAdded && !requireActivity().isFinishing) {
                showBottomSheetDialog()
            }
        }
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

            tvForgot.setOnClickListener {
                navigate(R.id.login_to_forgot)
                bottomSheetDialog.dismiss()
            }

            btnBack.setOnClickListener {
                navigate(R.id.login_to_onboarding)
                bottomSheetDialog.dismiss()
            }

            btnLogin.setOnClickListener {
                email = tietEmail.text.toString().trim()
                password = tietPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                userViewModel.loginUser(email, password)
            }

            userViewModel.authLogin.observe(viewLifecycleOwner) { result ->
                if (result.first) {
                    userViewModel.completeUserRegistration(requireContext())
                } else {
                    Toast.makeText(requireContext(), result.second ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                }
            }

            userViewModel.authStatus.observe(viewLifecycleOwner) { result ->
                if (result.first) {
                    Toast.makeText(requireContext(), result.second ?: "Login sukses", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        loginPreferences.setLoginStatus(true)
                        userAccountPreferences.setRegistered(false)
                    }
                    navigateToMainActivity()
                } else {
                    Toast.makeText(requireContext(), result.second ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
