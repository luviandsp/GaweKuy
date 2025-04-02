package com.gawebersama.gawekuy.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.enum.UserRole
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.ActivityProfileUserBinding
import com.gawebersama.gawekuy.ui.portfolio.PortfolioActivity

class ProfileUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileUserBinding
    private val userViewModel by viewModels<UserViewModel>()

    companion object {
        const val TAG = "ProfileUserActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        observeViewModels()
    }
    private fun initViews() {
        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun observeViewModels() {
        userViewModel.apply {
            userName.observe(this@ProfileUserActivity) { name ->
                if (name != null) {
                    binding.tvNickname.text = name
                } else {
                    binding.tvNickname.setText(R.string.user)
                }
            }

            userStatus.observe(this@ProfileUserActivity) { status ->
                binding.tvStatus.text = status ?: ""
            }

            userImageUrl.observe(this@ProfileUserActivity) { imageUrl ->
                Glide.with(this@ProfileUserActivity)
                    .load(imageUrl.takeUnless { it.isNullOrEmpty() } ?: R.drawable.user_circle)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            }

            userBiography.observe(this@ProfileUserActivity) { bio ->
                binding.tvUserBio.text = bio ?: ""
            }

            userRole.observe(this@ProfileUserActivity) { role ->
                with(binding) {
                    if (role != UserRole.FREELANCER.toString()) {
                        btnChat.visibility = View.GONE
                        llPortfolio.visibility = View.GONE
                    } else {
                        btnChat.visibility = View.VISIBLE
                        llPortfolio.visibility = View.VISIBLE
                    }
                }
            }

            userPhone.observe(this@ProfileUserActivity) { phone ->
                binding.btnChat.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/$phone".toUri()))
                }
            }

            userId.observe(this@ProfileUserActivity) { userId ->
                binding.llPortfolio.setOnClickListener {
                    val intent = Intent(this@ProfileUserActivity, PortfolioActivity::class.java)
                    intent.putExtra(PortfolioActivity.USER_ID, userId)
                    startActivity(intent)
                }
            }
        }
    }
}