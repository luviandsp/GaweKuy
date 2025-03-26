package com.gawebersama.gawekuy.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gawebersama.gawekuy.data.auth.AuthRepository
import com.gawebersama.gawekuy.data.auth.ClientType
import com.gawebersama.gawekuy.data.viewmodel.AuthViewModel
import com.gawebersama.gawekuy.databinding.FragmentProfileBinding
import com.gawebersama.gawekuy.ui.auth.AuthActivity
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
        authViewModel.getUser()
        initViews()
    }

    private fun initViews() {
        with(binding) {
            authViewModel.userName.observe(viewLifecycleOwner) { name ->
                tvNickname.text = name ?: "Guest"
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

            }

            btnLogout.setOnClickListener {
                lifecycleScope.launch {
                    authViewModel.logoutUser()

                    val intent = Intent(requireActivity(), AuthActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}