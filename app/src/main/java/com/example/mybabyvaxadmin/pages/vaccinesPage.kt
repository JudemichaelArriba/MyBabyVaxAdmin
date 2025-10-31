package com.example.mybabyvaxadmin.pages

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.adapters.VaccineAdapter
import com.example.mybabyvaxadmin.databinding.FragmentVaccinesPageBinding
import com.example.mybabyvaxadmin.models.Vaccine
import com.example.mybabyvaxadmin.services.DatabaseService
import com.example.mybabyvaxadmin.interfaces.InterfaceClass
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
    private var allVaccines: MutableList<Vaccine> = mutableListOf()
    private lateinit var vaccineAdapter: VaccineAdapter
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVaccinesPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefresh.setColorSchemeColors(
            resources.getColor(R.color.mainColor)
        )
        setupSearchBar()
        setupAddButton()
        setupRecyclerView()
        setupSwipeRefresh()
        if (!isDataLoaded) fetchVaccines()
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
                searchInput.text?.clear()
                vaccineAdapter.updateList(allVaccines)
            }
            isExpanded = !isExpanded
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                val filtered = allVaccines.filter { vaccine ->
                    vaccine.name?.lowercase()!!.contains(query) ||
                            vaccine.type?.lowercase()!!.contains(query) ||
                            vaccine.route?.lowercase()!!.contains(query)
                }
                vaccineAdapter.updateList(filtered)
            }
        })
    }

    private fun setupAddButton() {
        binding.btnAddVaccination.setOnClickListener {
            startActivity(Intent(requireContext(), AddVaccinePage::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.vaccinesRecycleview.layoutManager = LinearLayoutManager(requireContext())
        vaccineAdapter = VaccineAdapter(allVaccines)
        binding.vaccinesRecycleview.adapter = vaccineAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchVaccines()
        }
    }

    private fun fetchVaccines() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                val vaccines = getAllVaccinesSafe()
                if (isAdded) {
                    allVaccines.clear()
                    allVaccines.addAll(vaccines)
                    vaccineAdapter.updateList(allVaccines)
                    isDataLoaded = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private suspend fun getAllVaccinesSafe(): List<Vaccine> =
        suspendCancellableCoroutine { cont ->
            databaseService.fetchAllVaccines(object : InterfaceClass.VaccineListCallback {
                override fun onVaccinesLoaded(vaccines: List<Vaccine>) {
                    if (cont.isActive) cont.resume(vaccines)
                }

                override fun onError(message: String) {
                    if (cont.isActive) cont.resumeWithException(Exception(message))
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
