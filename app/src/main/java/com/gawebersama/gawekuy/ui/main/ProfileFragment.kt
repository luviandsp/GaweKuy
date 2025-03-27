package com.gawebersama.gawekuy.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.enum.ClientType
import com.gawebersama.gawekuy.data.viewmodel.AuthViewModel
import com.gawebersama.gawekuy.databinding.CustomLogoutDialogBinding
import com.gawebersama.gawekuy.databinding.FragmentProfileBinding
import com.gawebersama.gawekuy.ui.auth.AuthActivity
import com.gawebersama.gawekuy.ui.profile.EditProfileActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.srlProfile.setOnRefreshListener {
            refreshProfileData()
        }

        initViews()
    }

    private fun initViews() {
        with(binding) {
            authViewModel.userName.observe(viewLifecycleOwner) { name ->
                tvNickname.text = name ?: ""
            }

            authViewModel.userImageUrl.observe(viewLifecycleOwner) { imageUrl ->
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .circleCrop()
                        .into(ivProfilePicture)
                } else {
                    Glide.with(requireContext())
                        .load(R.drawable.user_circle)
                        .circleCrop()
                        .into(ivProfilePicture)
                }
            }

            authViewModel.userRole.observe(viewLifecycleOwner) { role ->
                if (role == ClientType.FREELANCER.toString()) {
                    btnBecomeFreelance.visibility = View.GONE

                    val params = btnEditProfile.layoutParams as LinearLayout.LayoutParams
                    params.weight = 2f
                    btnEditProfile.layoutParams = params
                }
            }

            btnEditProfile.setOnClickListener {
                val intent = Intent(requireActivity(), EditProfileActivity::class.java)
                startActivity(intent)
            }

            trLogout.setOnClickListener {
                showLogoutDialog()
            }
        }
    }

    private fun showLogoutDialog() {
        val dialogBinding = CustomLogoutDialogBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.btnLogout.setOnClickListener {
            logoutUser()
            alertDialog.dismiss()
        }

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            authViewModel.logoutUser()

            val intent = Intent(requireActivity(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun refreshProfileData() {

        authViewModel.getUser()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.srlProfile.isRefreshing = false // Hentikan animasi refresh setelah selesai
        }, 2000) // Simulasi delay 2 detik
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}