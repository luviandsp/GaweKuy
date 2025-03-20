package com.gawebersama.gawekuy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.auth.AuthRepository
import com.gawebersama.gawekuy.databinding.BottomSheetDialogRegisterBinding
import com.gawebersama.gawekuy.databinding.FragmentRegisterBinding
import com.gawebersama.gawekuy.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import kotlin.getValue


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    val authRepository = AuthRepository()

    val args: RegisterFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showBottomSheetDialog()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetDialogRegisterBinding.inflate(layoutInflater)

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
            if(args.clientType == 0) {
                tvText.setText(R.string.register_text_client)
            } else {
                tvText.setText(R.string.register_text_freelancer)
            }

            btnRegister.setOnClickListener {
                val email = tietEmail.text.toString()
                val password = tietPassword.text.toString()
                val confirmPassword = tietConfirmPassword.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password.length < 8) {
                    Toast.makeText(activity, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!password.any { it.isUpperCase() }) {
                    Toast.makeText(activity, "Password harus mengandung huruf kapital", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!password.any { it.isLowerCase() }) {
                    Toast.makeText(activity, "Password harus mengandung huruf kecil", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!password.any { it.isDigit() }) {
                    Toast.makeText(activity, "Password harus mengandung angka", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password != confirmPassword) {
                    Toast.makeText(activity, "Password tidak sama", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    isLoggedIn = authRepository.register(email, password)

                    if (isLoggedIn) {
                        navigateToMainActivity()
                    }
                }
            }

            tvLogin.setOnClickListener {
                val navController = view?.findNavController()
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.slide_in_right)
                    .setPopExitAnim(R.anim.slide_out_left)
                    .setPopUpTo(R.id.registerFragment, true)
                    .build()

                navController?.navigate(R.id.register_to_login, null, navOptions)
                bottomSheetDialog.dismiss()
            }

            btnBack.setOnClickListener {
                val navController = view?.findNavController()
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.slide_in_right)
                    .setPopExitAnim(R.anim.slide_out_left)
                    .setPopUpTo(R.id.registerFragment, true)
                    .build()

                navController?.navigate(R.id.register_to_registerSelect, null, navOptions)
                bottomSheetDialog.dismiss()
            }

            tietPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val password = s.toString()

                    // Validasi panjang password
                    if (password.length >= 8) {
                        binding.iconLength.setImageResource(R.drawable.baseline_check_circle_24)
                    } else {
                        binding.iconLength.setImageResource(R.drawable.cross_circle)
                    }

                    // Validasi huruf kapital
                    if (password.any { it.isUpperCase() }) {
                        binding.iconUppercase.setImageResource(R.drawable.baseline_check_circle_24)
                    } else {
                        binding.iconUppercase.setImageResource(R.drawable.cross_circle)
                    }

                    // Validasi huruf kecil
                    if (password.any { it.isLowerCase() }) {
                        binding.iconLowercase.setImageResource(R.drawable.baseline_check_circle_24)
                    } else {
                        binding.iconLowercase.setImageResource(R.drawable.cross_circle)
                    }

                    // Validasi angka
                    if (password.any { it.isDigit() }) {
                        binding.iconNumber.setImageResource(R.drawable.baseline_check_circle_24)
                    } else {
                        binding.iconNumber.setImageResource(R.drawable.cross_circle)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
