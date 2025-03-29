package com.gawebersama.gawekuy.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.gawebersama.gawekuy.ui.profile.EditProfileActivity
import com.gawebersama.gawekuy.ui.profile.FavoriteFreelancerActivity
import com.gawebersama.gawekuy.ui.profile.MyServiceActivity
import com.gawebersama.gawekuy.ui.profile.ProjectHistoryActivity
import com.gawebersama.gawekuy.ui.profile.SettingActivity
import kotlinx.coroutines.launch
import kotlin.jvm.java

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by viewModels<UserViewModel>()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == EditProfileActivity.RESULT_CODE) {
            refreshProfileData()
        }
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
            initViews()
            observeViewModels()
        }

        initViews()
        observeViewModels()
    }

    private fun initViews() {
        with(binding) {
            btnEditProfile.setOnClickListener {
                val intent = Intent(requireActivity(), EditProfileActivity::class.java)
                launcher.launch(intent)
            }

            btnMyProject.setOnClickListener {
                val intent = Intent(requireActivity(), MyServiceActivity::class.java)
                startActivity(intent)
            }

            btnBecomeFreelance.setOnClickListener {
                showFreelancerDialog()
            }

            trSetting.setOnClickListener {
                val intent = Intent(requireActivity(), SettingActivity::class.java)
                startActivity(intent)
            }

            trHistory.setOnClickListener {
                val intent = Intent(requireActivity(), ProjectHistoryActivity::class.java)
                startActivity(intent)
            }

            trFavorites.setOnClickListener {
                val intent = Intent(requireActivity(), FavoriteFreelancerActivity::class.java)
                startActivity(intent)
            }

            trReview.setOnClickListener {

            }

            trLogout.setOnClickListener {
                showLogoutDialog()
            }
        }
    }

    private fun observeViewModels() {
        with (binding) {
            userViewModel.apply {
                userName.observe(viewLifecycleOwner) { name ->
                    tvNickname.text = name ?: ""
                }

                userStatus.observe(viewLifecycleOwner) { status ->
                    tvStatus.text = status ?: ""
                }

                userImageUrl.observe(viewLifecycleOwner) { imageUrl ->
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

                userRole.observe(viewLifecycleOwner) { role ->
                    if (role == UserRole.FREELANCER.toString()) {
                        btnBecomeFreelance.visibility = View.GONE
                        btnMyProject.visibility = View.VISIBLE
                    } else {
                        btnBecomeFreelance.visibility = View.VISIBLE
                        btnMyProject.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showFreelancerDialog() {
        val dialogBinding = DialogFreelancerBinding.inflate(layoutInflater)
        val freelanceDialog = AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.btnConfirm.setOnClickListener {
            becomeFreelancer()
            freelanceDialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            freelanceDialog.dismiss()
        }

        freelanceDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        freelanceDialog.show()
    }

    private fun showLogoutDialog() {
        val dialogBinding = DialogLogoutBinding.inflate(layoutInflater)
        val logoutDialog = AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.btnCancel.setOnClickListener {
            logoutDialog.dismiss()
        }

        dialogBinding.btnLogout.setOnClickListener {
            logoutUser()
            logoutDialog.dismiss()
        }

        logoutDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        logoutDialog.show()
    }

    private fun becomeFreelancer() {
        lifecycleScope.launch {
            userViewModel.becomeFreelancer()
        }

        Toast.makeText(requireContext(), "Anda berhasil menjadi freelancer", Toast.LENGTH_SHORT).show()

        refreshProfileData()
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            userViewModel.logoutUser()

            val intent = Intent(requireActivity(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun refreshProfileData() {
        userViewModel.getUser()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.srlProfile.isRefreshing = false // Hentikan animasi refresh setelah selesai
        }, 2000) // Simulasi delay 2 detik
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}