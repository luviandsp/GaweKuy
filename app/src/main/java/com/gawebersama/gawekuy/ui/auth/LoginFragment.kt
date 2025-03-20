package com.gawebersama.gawekuy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.auth.AuthRepository
import com.gawebersama.gawekuy.databinding.BottomSheetDialogLoginBinding
import com.gawebersama.gawekuy.databinding.FragmentLoginBinding
import com.gawebersama.gawekuy.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showBottomSheetDialog()

        if (authRepository.isLoggedIn()) {
            navigateToMainActivity()
        }
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetDialogLoginBinding.inflate(layoutInflater)

        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setCancelable(false)

        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            d.window?.setDimAmount(0f)
        }

        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()

        var isLoggedIn = authRepository.isLoggedIn()

        with(binding) {
            tvRegister.setOnClickListener {
                bottomSheetDialog.dismiss()

                val navController = view?.findNavController()
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build()

                navController?.navigate(R.id.login_to_register, null, navOptions)
            }

            btnBack.setOnClickListener {
                bottomSheetDialog.dismiss()

                val navController = view?.findNavController()
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.slide_in_right)
                    .setPopExitAnim(R.anim.slide_out_left)
                    .setPopUpTo(R.id.loginFragment, true)
                    .build()

                navController?.navigate(R.id.login_to_onboarding, null, navOptions)
            }

            btnLogin.setOnClickListener {
                val email = tietEmail.text.toString()
                val password = tietPassword.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    isLoggedIn = authRepository.login(email, password)

                    if (isLoggedIn) {
                        navigateToMainActivity()
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
