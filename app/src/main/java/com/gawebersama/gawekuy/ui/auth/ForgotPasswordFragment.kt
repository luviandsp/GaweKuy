package com.gawebersama.gawekuy.ui.auth

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
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.BottomSheetDialogForgotBinding
import com.gawebersama.gawekuy.databinding.FragmentForgotPasswordBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import kotlin.getValue

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showBottomSheetDialog()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetDialogForgotBinding.inflate(layoutInflater)

        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setOnShowListener { dialog ->
            (dialog as BottomSheetDialog).window?.setDimAmount(0f)
        }

        bottomSheetDialog.setContentView(sheetBinding.root)
        bottomSheetDialog.show()

        with(sheetBinding) {
            btnBack.setOnClickListener {
                navigate(R.id.forgot_to_login)
                bottomSheetDialog.dismiss()
            }

            btnChangePassword.setOnClickListener {
                val email = tietEmail.text.toString().trim()

                if (email.isEmpty()) {
                    Toast.makeText(activity, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    userViewModel.forgotPassword(email)
                }
            }
        }
    }

    private fun navigate(destination: Int) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.forgotPasswordFragment, true)
            .build()
        view?.findNavController()?.navigate(destination, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}