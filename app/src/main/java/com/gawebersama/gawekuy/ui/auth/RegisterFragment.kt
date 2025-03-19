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
import androidx.navigation.findNavController
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.auth.AuthRepository
import com.gawebersama.gawekuy.databinding.FragmentRegisterBinding
import com.gawebersama.gawekuy.ui.main.MainActivity
import kotlinx.coroutines.launch


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    val authRepository = AuthRepository()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            view?.findNavController()?.popBackStack(R.id.loginFragment, false)
        }
    }

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
        initViews()
    }

    private fun initViews() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        var isLoggedIn = authRepository.isLoggedIn()

        with(binding) {
            btnRegister.setOnClickListener {
                val email = tietEmail.text.toString()
                val password = tietPassword.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity, "Email dan Password wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    isLoggedIn = authRepository.register(email, password)

                    if (isLoggedIn) {
                        navigateToMainActivity()
                    }
                }
            }

            btnLogin.setOnClickListener {
                view?.findNavController()?.navigate(R.id.register_to_login)
            }

            btnInputData.setOnClickListener {
                view?.findNavController()?.navigate(R.id.register_to_inputData)
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}