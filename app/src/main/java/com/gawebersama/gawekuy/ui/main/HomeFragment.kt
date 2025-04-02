package com.gawebersama.gawekuy.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.CategoryAdapter
import com.gawebersama.gawekuy.data.adapter.ServiceAdapter
import com.gawebersama.gawekuy.data.datamodel.CategoryModel
import com.gawebersama.gawekuy.data.enum.FilterAndOrderService
import com.gawebersama.gawekuy.data.viewmodel.ServiceViewModel
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.FragmentHomeBinding
import com.gawebersama.gawekuy.ui.service.ServiceDetailActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by viewModels<UserViewModel>()
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private lateinit var serviceAdapter: ServiceAdapter


    companion object {
        const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.srlHome.setOnRefreshListener {
            refreshHomeData()
        }

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        with (binding) {
            serviceAdapter = ServiceAdapter(
                onItemClick = { service ->
                    val intent = Intent(requireContext(), ServiceDetailActivity::class.java)
                    intent.putExtra(ServiceDetailActivity.SERVICE_ID, service.serviceId)
                    startActivity(intent)
                },
                onHoldClick = { }
            )

            rvFreelancer.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = serviceAdapter
            }
        }

        categoryRV()
    }

    private fun observeViewModel() {
        userViewModel.userName.observe(viewLifecycleOwner) { name ->
            if (name != null) {
                binding.tvName.text = name
            } else {
                binding.tvName.setText(R.string.user)
            }
        }

        serviceViewModel.serviceWithUser.observe(viewLifecycleOwner) { services ->
            Log.d(TAG, "Fetched Services: $services")
            serviceAdapter.submitList(services)
            binding.srlHome.isRefreshing = false
        }
    }

    private fun categoryRV() {
        val daftarCategoryModels = listOf(
            CategoryModel("Penulisan & Akademik", R.drawable.academic_category),
            CategoryModel("Pengembangan Teknologi", R.drawable.computer_category),
            CategoryModel("Desain & Multimedia", R.drawable.design_category),
            CategoryModel("Pemasaran & Media Sosial", R.drawable.marketing_category),
            CategoryModel("Lainnya", R.drawable.others_category)
        )

        with(binding) {
            val rvAdapter = CategoryAdapter(daftarCategoryModels).apply {
                setOnItemClickListener(object : CategoryAdapter.OnItemClickListener {
                    override fun onItemClick(categoryModel: CategoryModel) {
                        Toast.makeText(requireContext(), "Kategori: ${categoryModel.name}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            rvCategory.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                setHasFixedSize(true)
                adapter = rvAdapter
            }
        }
    }

    private fun refreshHomeData() {
        binding.srlHome.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                serviceViewModel.fetchAllServices(FilterAndOrderService.RATING)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching services: ${e.message}")
            } finally {
                binding.srlHome.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHomeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}