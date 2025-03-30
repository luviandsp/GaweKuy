package com.gawebersama.gawekuy.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.enum.UserRole
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.DialogFreelancerBinding
import com.gawebersama.gawekuy.databinding.DialogLogoutBinding
import com.gawebersama.gawekuy.databinding.FragmentProfileBinding
import com.gawebersama.gawekuy.ui.auth.AuthActivity
import com.gawebersama.gawekuy.ui.profile.*
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by viewModels<UserViewModel>()

    private val launcherEditActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == EditProfileActivity.RESULT_CODE) {
            refreshProfileData()
        }
    }

    companion object {
        const val TAG = "ProfileFragment"
    }

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
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            btnEditProfile.setOnClickListener {
                launcherEditActivity.launch(Intent(requireActivity(), EditProfileActivity::class.java))
            }

            btnMyProject.setOnClickListener {
                startActivity(Intent(requireActivity(), MyServiceActivity::class.java))
            }

            btnBecomeFreelance.setOnClickListener {
                showFreelancerDialog()
            }

            trSetting.setOnClickListener {
                startActivity(Intent(requireActivity(), SettingActivity::class.java))
            }

            trHistory.setOnClickListener {
                startActivity(Intent(requireActivity(), ProjectHistoryActivity::class.java))
            }

            trFavorites.setOnClickListener {
                startActivity(Intent(requireActivity(), FavoriteFreelancerActivity::class.java))
            }

            trLogout.setOnClickListener {
                showLogoutDialog()
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            userName.observe(viewLifecycleOwner) { name ->
                binding.tvNickname.text = name ?: ""
            }

            userStatus.observe(viewLifecycleOwner) { status ->
                binding.tvStatus.text = status ?: ""
            }

            userImageUrl.observe(viewLifecycleOwner) { imageUrl ->
                Glide.with(this@ProfileFragment)
                    .load(imageUrl.takeUnless { it.isNullOrEmpty() } ?: R.drawable.user_circle)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            }

            userRole.observe(viewLifecycleOwner) { role ->
                if (role == UserRole.FREELANCER.toString()) {
                    binding.btnBecomeFreelance.visibility = View.GONE
                    binding.btnMyProject.visibility = View.VISIBLE
                } else {
                    binding.btnBecomeFreelance.visibility = View.VISIBLE
                    binding.btnMyProject.visibility = View.GONE
                }
            }
        }
    }

    private fun showFreelancerDialog() {
        val dialogBinding = DialogFreelancerBinding.inflate(layoutInflater)
        val freelanceDialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()

        with(dialogBinding) {
            btnConfirm.setOnClickListener {
                becomeFreelancer()
                freelanceDialog.dismiss()
            }

            btnCancel.setOnClickListener {
                freelanceDialog.dismiss()
            }

            freelanceDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            freelanceDialog.show()
        }
    }

    private fun showLogoutDialog() {
        val dialogBinding = DialogLogoutBinding.inflate(layoutInflater)
        val logoutDialog = AlertDialog.Builder(requireContext()).setView(dialogBinding.root).create()

        with(dialogBinding) {
            btnCancel.setOnClickListener {
                logoutDialog.dismiss()
            }

            btnLogout.setOnClickListener {
                logoutUser()
                logoutDialog.dismiss()
            }

            logoutDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            logoutDialog.show()
        }
    }

    private fun becomeFreelancer() {
        lifecycleScope.launch {
            userViewModel.becomeFreelancer()
            Toast.makeText(requireContext(), "Anda berhasil menjadi freelancer", Toast.LENGTH_SHORT).show()
            refreshProfileData()
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            userViewModel.logoutUser()
            startActivity(Intent(requireActivity(), AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun refreshProfileData() {
        binding.srlProfile.isRefreshing = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                userViewModel.getUser()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user data: ${e.message}")
            } finally {
                binding.srlProfile.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshProfileData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}