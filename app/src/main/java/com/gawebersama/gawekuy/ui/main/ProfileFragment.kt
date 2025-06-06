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
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.datastore.LoginPreferences
import com.gawebersama.gawekuy.data.enum.UserRole
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.DialogFreelancerBinding
import com.gawebersama.gawekuy.databinding.DialogLogoutBinding
import com.gawebersama.gawekuy.databinding.FragmentProfileBinding
import com.gawebersama.gawekuy.ui.auth.AuthActivity
import com.gawebersama.gawekuy.ui.portfolio.PortfolioActivity
import com.gawebersama.gawekuy.ui.profile.*
import com.gawebersama.gawekuy.ui.service.MyServiceActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by viewModels<UserViewModel>()
    private lateinit var loginPreferences: LoginPreferences

    private val launcherEditActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == EditProfileActivity.RESULT_CODE) {
            refreshProfileData()
        }
    }

    companion object {
        private const val TAG = "ProfileFragment"
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

        loginPreferences = LoginPreferences(requireContext())
        refreshProfileData()

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            trEditProfile.setOnClickListener {
                launcherEditActivity.launch(Intent(requireActivity(), EditProfileActivity::class.java))
            }

            llSeeProfile.setOnClickListener {
                startActivity(Intent(requireActivity(), ProfileUserActivity::class.java))
            }

            btnBecomeFreelance.setOnClickListener {
                showFreelancerDialog()
            }

            trMyService.setOnClickListener {
                startActivity(Intent(requireActivity(), MyServiceActivity::class.java))
            }

            trSetting.setOnClickListener {
                startActivity(Intent(requireActivity(), SettingActivity::class.java))
            }

//            trHistory.setOnClickListener {
//                startActivity(Intent(requireActivity(), OrderHistoryActivity::class.java))
//            }

//            trFavorites.setOnClickListener {
//                startActivity(Intent(requireActivity(), FavoriteFreelancerActivity::class.java))
//            }

            trMyPayment.setOnClickListener {
                startActivity(Intent(requireActivity(), PaymentOptionActivity::class.java))
            }

            trCheckUpdates.setOnClickListener {
                val updateIntent = Intent(Intent.ACTION_VIEW, "https://github.com/luviandsp/GaweKuy/releases".toUri())
                startActivity(updateIntent)
            }

            trLogout.setOnClickListener {
                showLogoutDialog()
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            userName.observe(viewLifecycleOwner) { name ->
                if (name != null) {
                    binding.tvNickname.text = name
                } else {
                    binding.tvNickname.setText(R.string.user)
                }
            }

            userStatus.observe(viewLifecycleOwner) { status ->
                if (status != "") {
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = status
                } else {
                    binding.tvStatus.visibility = View.GONE
                }
            }

            userImageUrl.observe(viewLifecycleOwner) { imageUrl ->
                Glide.with(this@ProfileFragment)
                    .load(imageUrl.takeUnless { it.isNullOrEmpty() } ?: R.drawable.user_circle)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            }

            userRole.observe(viewLifecycleOwner) { role ->
                with(binding) {
                    if (role == UserRole.FREELANCER.toString()) {
                        btnBecomeFreelance.visibility = View.GONE
                        trMyPortfolio.visibility = View.VISIBLE
                        trMyService.visibility = View.VISIBLE
                        trMyPayment.visibility = View.VISIBLE
                        dividerFreelancerService.visibility = View.VISIBLE
                        dividerFreelancerPortfolio.visibility = View.VISIBLE
                        dividerFreelancerPayment.visibility = View.VISIBLE
                    } else {
                        btnBecomeFreelance.visibility = View.VISIBLE
                        trMyPortfolio.visibility = View.GONE
                        trMyService.visibility = View.GONE
                        trMyPayment.visibility = View.GONE
                        dividerFreelancerService.visibility = View.GONE
                        dividerFreelancerPortfolio.visibility = View.GONE
                        dividerFreelancerPayment.visibility = View.GONE
                    }
                }
            }

            userId.observe(viewLifecycleOwner) { userId ->
                binding.trMyPortfolio.setOnClickListener {
                    val intent = Intent(requireActivity(), PortfolioActivity::class.java)
                    intent.putExtra(PortfolioActivity.SOURCE_INTENT, "profileFragment")
                    intent.putExtra(PortfolioActivity.USER_ID, userId)
                    startActivity(intent)
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
                lifecycleScope.launch {
                    loginPreferences.setLoginStatus(false)
                }

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
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                userViewModel.getUser()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user data: ${e.message}")
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