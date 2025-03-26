package com.gawebersama.gawekuy.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.auth.ClientType
import com.gawebersama.gawekuy.databinding.BottomSheetDialogRegisterSelectorBinding
import com.gawebersama.gawekuy.databinding.FragmentRegisterSelectBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class RegisterSelectFragment : Fragment() {

    private var _binding: FragmentRegisterSelectBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showBottomSheetDialog()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetDialogRegisterSelectorBinding.inflate(layoutInflater)

        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setCancelable(false)

        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            d.window?.setDimAmount(0f)
        }

        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()

        with(binding) {
            btnRegisterClient.setOnClickListener {
                bottomSheetDialog.dismiss()
                toRegisterFragment(ClientType.CLIENT)
            }

            btnRegisterFreelancer.setOnClickListener {
                bottomSheetDialog.dismiss()
                toRegisterFragment(ClientType.FREELANCER)
            }

            tvLogin.setOnClickListener {
                val navController = view?.findNavController()
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.registerFragment, true)
                    .build()

                navController?.navigate(R.id.registerSelect_to_login, null, navOptions)
                bottomSheetDialog.dismiss()
            }

            btnBack.setOnClickListener {
                val navController = view?.findNavController()
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right)
                    .setPopEnterAnim(R.anim.slide_in_right)
                    .setPopExitAnim(R.anim.slide_out_left)
                    .setPopUpTo(R.id.loginFragment, true)
                    .build()

                navController?.navigate(R.id.registerSelect_to_onboarding, null, navOptions)
                bottomSheetDialog.dismiss()
            }
        }
    }

    private fun toRegisterFragment(clientType: ClientType) {
        val action = RegisterSelectFragmentDirections.registerSelectToRegister(clientType)
        view?.findNavController()?.navigate(action, animation())
    }

    private fun animation() = navOptions {
        anim {
            enter = R.anim.slide_in_right
            exit = R.anim.slide_out_left
            popEnter = R.anim.slide_in_left
            popExit = R.anim.slide_out_right
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}