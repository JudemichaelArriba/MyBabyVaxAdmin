package com.example.mybabyvaxadmin.pages

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybabyvaxadmin.adapters.VaccineAdapter
import com.example.mybabyvaxadmin.databinding.FragmentVaccinesPageBinding
import com.example.mybabyvaxadmin.models.Vaccine
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.interfaces.InterfaceClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class vaccinesPage : Fragment() {

    private var _binding: FragmentVaccinesPageBinding? = null
    private val binding get() = _binding!!

    private var isExpanded = false
    private val databaseService = DatabaseService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaccinesPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchBar()
        setupAddButton()
        setupRecyclerView()
        fetchVaccines()
    }

    private fun setupSearchBar() {
        val searchContainer = binding.searchContainer
        val searchIcon = binding.searchIcon
        val searchInput = binding.searchInput

        searchIcon.setOnClickListener {
            if (!isExpanded) {
                expandSearchBar(searchContainer)
                searchInput.visibility = View.VISIBLE
                searchInput.alpha = 0f
                searchInput.animate().alpha(1f).setDuration(200).start()
            } else {
                collapseSearchBar(searchContainer)
                searchInput.animate().alpha(0f).setDuration(200)
                    .withEndAction { searchInput.visibility = View.GONE }
                    .start()
            }
            isExpanded = !isExpanded
        }
    }

    private fun setupAddButton() {
        binding.btnAddVaccination.setOnClickListener {
            startActivity(Intent(requireContext(), AddVaccinePage::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.vaccinesRecycleview.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchVaccines() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val vaccines = getAllVaccines()
                binding.vaccinesRecycleview.adapter = VaccineAdapter(vaccines)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun getAllVaccines(): List<Vaccine> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchAllVaccines(object : InterfaceClass.VaccineListCallback {
                override fun onVaccinesLoaded(vaccines: List<Vaccine>) {
                    cont.resume(vaccines)
                }

                override fun onError(message: String) {
                    cont.resumeWithException(Exception(message))
                }


            })
        }

    private fun expandSearchBar(view: View) {
        val startWidth = view.width
        val endWidth = 600

        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            val params = view.layoutParams
            params.width = value
            view.layoutParams = params
        }
        animator.duration = 300
        animator.start()
    }

    private fun collapseSearchBar(view: View) {
        val startWidth = view.width
        val endWidth = 120

        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            val params = view.layoutParams
            params.width = value
            view.layoutParams = params
        }
        animator.duration = 300
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
