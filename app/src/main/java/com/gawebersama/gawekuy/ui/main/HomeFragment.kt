package com.gawebersama.gawekuy.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gawebersama.gawekuy.R
import com.gawebersama.gawekuy.data.adapter.CategoryAdapter
import com.gawebersama.gawekuy.data.adapter.FreelancerServiceAdapter
import com.gawebersama.gawekuy.data.dataclass.CategoryModel
import com.gawebersama.gawekuy.data.dataclass.FreelancerServiceModel
import com.gawebersama.gawekuy.data.viewmodel.UserViewModel
import com.gawebersama.gawekuy.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val userViewModel by viewModels<UserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {

        with (binding) {
            userViewModel.userName.observe(viewLifecycleOwner) { name ->
                binding.tvName.text = name ?: "Guest"
            }
        }

        categoryRV()
        serviceRV()
    }

    private fun serviceRV() {
        val daftarService = listOf(
            FreelancerServiceModel(
                name = "Luvian Daffa",
                title = "Pemrograman Android Berbasis Kotlin",
                rating = 5.0,
                image = R.drawable.logo,
                price = 120000.0
            ),
            FreelancerServiceModel(
                name = "Bima Adnandita",
                title = "Video Editing untuk Youtube dan Instagram",
                rating = 5.0,
                image = R.drawable.logo,
                price = 50000.0
            ),
            FreelancerServiceModel(
                name = "Luvian Daffa",
                title = "Video Editing untuk Youtube dan Instagram",
                rating = 5.0,
                image = R.drawable.logo,
                price = 50000.0
            ),
            FreelancerServiceModel(
                name = "Luvian Syauki",
                title = "Video Editing untuk Youtube dan Instagram",
                rating = 5.0,
                image = R.drawable.logo,
                price = 50000.0
            ),
            FreelancerServiceModel(
                name = "Luvian Daffa",
                title = "Video Editing untuk Youtube dan Instagram",
                rating = 5.0,
                image = R.drawable.logo,
                price = 50000.0
            )
        )

        with(binding) {
            val rvAdapter = FreelancerServiceAdapter(daftarService).apply {
                setOnItemClickListener(object : FreelancerServiceAdapter.OnItemClickListener {
                    override fun onItemClick(service: FreelancerServiceModel) {
                        Toast.makeText(
                            requireContext(),
                            "Jasa: ${service.title}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }

            rvFreelancer.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = rvAdapter
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}